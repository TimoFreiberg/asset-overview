(ns asset-overview.repository
  (:require [asset-overview.config :refer [config]]
            [cheshire.core :as json]
            [clj-jgit.porcelain :as git]
            [clojure.java.io :as io]
            [mount.core :as mount :refer [defstate]]))

(defn create-temp-dir
  [dir-prefix]
  (let [temp-dir (-> (java.nio.file.Files/createTempDirectory
                      dir-prefix
                      (into-array
                       java.nio.file.attribute.FileAttribute
                       []))
                     .toFile
                     io/file)
        _ .deleteOnExit temp-dir]
    temp-dir))

(defstate
  ^{:on-reload :noop}
  git-repo
  :start (let
             [temp-repo-dir (create-temp-dir "asset-overview-data")
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
       (filter pred)
       (sort-by :name)
       reverse))

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
