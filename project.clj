(defproject asset-overview "0.1.0-SNAPSHOT"
  :description "Renders overviews of open source projects of NovaTec GmbH"
  :url "TODO"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [irresponsible/thyroid "0.1.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [clj-jgit "0.8.10"]
                 [mount "0.1.12"]
                 [cheshire "5.8.0"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler asset-overview.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
