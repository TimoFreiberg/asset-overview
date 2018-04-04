(ns asset-overview.handler
  (:require [compojure.core :as comp]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [irresponsible.thyroid :as thyroid]
            [clojure.java.io :as io]
            [mount.core :as mount :refer [defstate]]
            [clj-time.core :as time]
            [clj-jgit.porcelain :as git]
            [cheshire.core :as json]))

(defn load-config
  "Loads the resource config.edn.
  Throws an exception if the resource was not found. "
  [file-name]
  (with-open
    [reader
     (if-let [resource (io/resource file-name)]
       (-> resource
           io/reader
           java.io.PushbackReader.)
       (throw (Exception.
               (str "Resource " file-name " not found"))))]
    (clojure.edn/read reader)))

(defstate config
  :start (load-config "config.edn"))

(defstate
  ^{:on-reload :noop}
  git-repo
  :start (let
             [temp-repo-dir (->
                             (java.nio.file.Files/createTempDirectory
                              "asset-overview-data"
                              (into-array java.nio.file.attribute.FileAttribute []))
                             .toFile
                             io/file)
              _ (.deleteOnExit temp-repo-dir)
              remote-url (get-in config [:git :repository-url])
              repo (-> remote-url
                       (git/git-clone-full temp-repo-dir)
                       :repo)]
           {:repo repo
            :dir temp-repo-dir})
  :stop (io/delete-file (:dir git-repo)))

(defstate
  ^{:on-reload :noop}
  repo-data
  :start (do
           (doto (git-repo :repo)
             (git/git-fetch "origin")
             (git/git-checkout "master"))
           (-> git-repo
               :dir
               file-seq)))

(defn trim-extension
  "Returns the file name of a file without its extension.
  Does nothing to file names without an extension
  "
  [file]
  (let [file-name (.getName file)
        dot-pos (.lastIndexOf file-name ".")]
    (if (pos? dot-pos)
      (subs file-name 0 dot-pos)
      file-name)))

(defn get-filtered-projects
  "Loads projects from the repository that match the given predicate"
  [pred]
  (->> repo-data
       (filter #(.endsWith (.getName %) ".project"))
       (map
        #(-> %
             io/reader
             (json/parse-stream true)
             (assoc :id (trim-extension %))))
       (filter pred)))

(defn get-all-projects
  "Loads all projects from the repository."
  []
  (get-filtered-projects (fn [_] true)))

(defn get-project
  "Loads project with the given id from the repository.
  Returns nil if no project with that id exists."
  [id]
  (first
   (get-filtered-projects #(= id (:id %)))))

(def thymeleaf-resolver
  (thyroid/template-resolver
   {:type :file
    :prefix "resources/templates"
    :suffix ".html"
    :cache? false}))

(def thymeleaf-engine
  (thyroid/make-engine {:resolvers [thymeleaf-resolver]}))

(defn display-project [project]
  (thyroid/render
   thymeleaf-engine
   "project.html"
   {:project project}))

(defn display-projects [projects]
  (thyroid/render
   thymeleaf-engine
   "index.html"
   {:projects (remove :legacy projects)
    :legacyProjects (filter :legacy projects)}))

(comp/defroutes app-routes
  (comp/GET "/" [] (display-projects (get-all-projects)))
  (comp/GET "/:id" [id] (-> id
                            get-project
                            display-project))
  (route/not-found "Not Found, yo"))

(def app
  (wrap-defaults app-routes site-defaults))
