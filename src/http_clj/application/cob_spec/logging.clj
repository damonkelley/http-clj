(ns http-clj.application.cob-spec.logging
  (:require [http-clj.logging :as logging]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]))

(defrecord CobSpecLogger [config]
  logging/Logger
  (log [this level contents]
      (timbre/log* config level contents)))

(def default-config
  {:level :debug
   :appenders {:spit (appenders/spit-appender {:fname "resources/log/log.txt"})}})

(defn create
  ([] (create default-config))
  ([config] (->CobSpecLogger config)))
