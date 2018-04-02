(ns asset-overview.handler
  (:require [compojure.core :as comp]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [irresponsible.thyroid :as thyroid]
            [clojure.java.io :as io]))

(defn mk-example-project
  [id]
  {:id id
   :name "Test-Project"
   :organization "NovaTec GmbH"
   :repository "some-url.com"
   :documentation "No"
   :branches ["master" "release-1"]
   :services []
   :artifacts ["Test-Core" "Test-Utils" "Test-Compat"]
   :legacy false})

(defn repository
  [config]
  (assert
   (and (map? config)
        (get-in config [:git :repository-url])))
  {:config config :data nil})

(defn get-all-projects
  "Loads all projects from the repository."
  []
  [(mk-example-project 0)
   (mk-example-project 1)])

(defn get-project
  "Loads project with the given id from the repository.
  Returns nil if no project with that id exists."
  [id]
  (mk-example-project id))

(defn load-config
  "Loads the resource config.edn.
  Throws an exception if the resource was not found. "
  []
  (let [config-file-name "config.edn"]
    (with-open
      [reader (if-let [resource (io/resource config-file-name)]
                (-> resource
                    io/reader
                    java.io.PushbackReader.)
                (throw (Exception. (str "Resource " config-file-name " not found"))))]
      (clojure.edn/read reader))))

(def thymeleaf-resolver
  (thyroid/template-resolver
   {:type :file
    :prefix "resources/templates"
    :suffix ".html"
    :cache false}))

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
   {:projects projects}))

(comp/defroutes app-routes
  (comp/GET "/" [] (display-projects
                    (get-all-projects)))
  (comp/GET "/:id" [id] (-> id
                            get-project
                            display-project))
  (route/not-found "Not Found, yo"))

(def app
  (wrap-defaults app-routes site-defaults))

