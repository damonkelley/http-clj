(ns http-clj.application.cob-spec
  (:require [http-clj.router :refer [route]]
            [http-clj.server :refer [run]]
            [http-clj.application.cob-spec.handlers :as handlers]
            [http-clj.application.cob-spec.cli :as cli]
            [http-clj.application.cob-spec.logging :as logger]
            [clojure.java.io :as io]
            [taoensso.timbre.appenders.core :as appenders])
  (:import java.io.ByteArrayOutputStream))

(def log (ByteArrayOutputStream.))

(defn- cob-spec [request directory]
  (route
    request
    {["GET" "/log"] #(handlers/log % log)}
    :fallback #(handlers/fallback % directory)))

(defn app [directory]
  {:entrypoint #(cob-spec % directory)
   :logger (logger/create
             {:level :info
              :appenders {:println
                          (appenders/println-appender {:stream (io/writer log)})}})})

(defn -main [& args]
  (let [{options :options} (cli/cli args)]
    (run (app (:directory options)) (:port options))))
