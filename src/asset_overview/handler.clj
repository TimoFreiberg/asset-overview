(ns asset-overview.handler
  (:require [asset-overview.display :as display]
            [asset-overview.repository :as repo]
            [compojure.core :as comp]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(defn display-all-projects []
  (repo/ensure-repo-is-up-to-date)
  (display/display-projects (repo/get-all-projects)))

(defn display-project [id]
  (repo/ensure-repo-is-up-to-date)
  (-> id
      repo/get-project
      display/display-project))

(comp/defroutes app-routes
  (comp/GET "/" [] (display-all-projects))
  (comp/GET "/:id" [id] (display-project id))
  (route/not-found "Not Found, yo"))

(def app
  (-> app-routes
      (wrap-defaults site-defaults)
      (ring.middleware.content-type/wrap-content-type)
      (ring.middleware.resource/wrap-resource "static")))
