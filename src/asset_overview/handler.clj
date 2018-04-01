(ns asset-overview.handler
  (:require [compojure.core :as comp]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [clojure.pprint :refer [pprint]]
            [hiccup.core :as hic]))

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

(defn pprint-str [& rest]
  (with-out-str (pprint rest)))

(defn get-all-projects
  "Loads all projects from the repository."
  []
  [(mk-example-project 0)
   (mk-example-project 1)])

(defn get-project
  "Loads project with id `id` from the repository.
  Returns nil if no project with that id exists."
  [id]
  (mk-example-project id))

(defn display-project [project]
  (pprint-str project))

(defn display-projects [projects]
  (pprint-str
   (mapv display-project projects)))

(comp/defroutes app-routes
  (comp/GET "/" [] (display-projects
                    (get-all-projects)))
  (comp/GET "/:id" [id] (-> id
                            get-project
                            display-project))
  (route/not-found "Not Found, yo"))

(def app
  (wrap-defaults app-routes site-defaults))

