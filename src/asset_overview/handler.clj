(ns asset-overview.handler
  (:require [compojure.core :as comp]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [irresponsible.thyroid :as thyroid]))

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
  (mapv display-project projects))

(comp/defroutes app-routes
  (comp/GET "/" [] (display-projects
                    (get-all-projects)))
  (comp/GET "/:id" [id] (-> id
                            get-project
                            display-project))
  (route/not-found "Not Found, yo"))

(def app
  (wrap-defaults app-routes site-defaults))

