(ns asset-overview.config
  (:require [clojure.java.io :as io]
            [mount.core :refer [defstate]]))

(defn load-config
  "Loads the resource config.edn.
  Throws an exception if the resource was not found. "
  [file-name]
  (if-let [resource (io/resource file-name)]
    (-> resource
        slurp
        clojure.edn/read-string)
    (throw (ex-info
            (str "Resource " file-name " not found")))))

(defstate config
  :start (load-config "config.edn"))
