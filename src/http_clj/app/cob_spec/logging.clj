(ns http-clj.app.cob-spec.logging
  (:require [http-clj.logging :as logging]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            [clojure.string :as string]))

(defrecord CobSpecLogger [config]
  logging/Logger
  (log [this level contents]
      (timbre/log* config level contents)))

(defn- format-level [level]
  (-> level
    name
    string/upper-case
    (#(str "[" % "]"))))

(defn- format-message [messages]
  (apply str messages))

(defn- log-formatter [{:keys [level vargs]}]
  (let [level (format-level level)
        message (format-message vargs)]
    (str level " " message)))

(def default-config
  {:level :debug
   :output-fn log-formatter
   :appenders {:spit (appenders/spit-appender {:fname "resources/log/log.txt"})}})

(defn create
  ([] (create default-config))
  ([config] (->CobSpecLogger (merge default-config config))))
