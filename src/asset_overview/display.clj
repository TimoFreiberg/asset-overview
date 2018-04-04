(ns asset-overview.display
  (:require [irresponsible.thyroid :as thyroid]
            [clojure.string :as string]))

(def thymeleaf-resolver
  (thyroid/template-resolver
   {:type :file
    :prefix "resources/templates"
    :suffix ".html"
    :cache? false}))

(def thymeleaf-engine
  (thyroid/make-engine {:resolvers [thymeleaf-resolver]}))

(defn display-project [project]
  (let [{:keys [documentation branches artifacts services]} project]
    (thyroid/render
     thymeleaf-engine
     "project.html"
     {:project project
      :render_documentation (not (string/blank? documentation))
      :render_ci (seq branches)
      :render_artifacts (seq artifacts)
      :render_travis (.contains services "travis")
      :render_codecov (.contains services "codecov")
      :render_bettercode (.contains services "bettercode")})))

(defn display-projects [projects]
  (thyroid/render
   thymeleaf-engine
   "index.html"
   {:projects (remove :legacy projects)
    :legacyProjects (filter :legacy projects)}))
