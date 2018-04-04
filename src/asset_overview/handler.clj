(ns asset-overview.handler
  (:require [compojure.core :as comp]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [asset-overview.repository :as repo]
            [asset-overview.display :as display]))


(defn display-all-projects []
  (display/display-projects (repo/get-all-projects)))

(defn display-project [id]
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
