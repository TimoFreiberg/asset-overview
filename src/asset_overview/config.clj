(ns asset-overview.config
  (:require [clojure.java.io :as io]
            [mount.core :refer [defstate]]))

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
       (throw (ex-info
               (str "Resource " file-name " not found"))))]
    (clojure.edn/read reader)))

(defstate config
  :start (load-config "config.edn"))
