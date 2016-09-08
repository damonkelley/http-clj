(ns http-clj.app.cob-spec
  (:require [http-clj.router :refer [route GET POST OPTIONS PATCH PUT DELETE]]
            [http-clj.server :refer [run]]
            [http-clj.app.cob-spec.handlers :as handlers]
            [http-clj.request-handler :refer [auth]]
            [http-clj.app.cob-spec.cli :as cli]
            [http-clj.app.cob-spec.logging :as logger]
            [clojure.java.io :as io]
            [taoensso.timbre.appenders.core :as appenders])
  (:import java.io.ByteArrayOutputStream))

(def log (ByteArrayOutputStream.))
(def form-cache (atom ""))

(defn- cob-spec [request directory]
  (route
    request
    (-> []
       (GET "/logs"  (auth  #(handlers/log % log) "admin" "hunter2"))
       (POST "/form" #(handlers/submit-form % form-cache))
       (PUT "/form" #(handlers/submit-form % form-cache))
       (DELETE "/form" #(handlers/clear-submission % form-cache))
       (GET "/form" #(handlers/last-submission % form-cache))
       (GET "/parameters" handlers/parameters)
       (GET "/redirect" handlers/redirect-to-root)
       (OPTIONS "/method_options" (handlers/options "GET" "HEAD" "POST" "OPTIONS" "PUT"))
       (OPTIONS "/method_options2" (handlers/options "GET" "OPTIONS"))
       (GET #"^/.*$" #(handlers/static % directory))
       (PATCH #"^/.*$" #(handlers/static % directory)))))

(defn app [directory]
  {:entrypoint #(cob-spec % directory)
   :logger (logger/create
             {:level :info
              :appenders {:println
                          (appenders/println-appender {:stream (io/writer log)})}})})

(defn -main [& args]
  (let [{options :options} (cli/cli args)]
    (run (app (:directory options)) (:port options))))
