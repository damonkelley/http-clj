(ns http-clj.application.cob-spec.logging
  (:require [http-clj.logging :as logging]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]))

(defrecord CobSpecLogger [config]
  logging/Logger
  (log [this contents]
      (timbre/log* config :info contents)))

(defn default-config [stream]
  {:level :level
   :appenders {:spit (appenders/spit-appender {:fname "resources/log/log.txt"})}})

(defn create
  ([] (create default-config))
  ([config] (->CobSpecLogger config)))
