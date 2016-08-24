(ns http-clj.lifecycle
  (:require [http-clj.request :refer [create]]
            [http-clj.response :as response]
            [http-clj.connection :as connection]
            [http-clj.logging :as logging]))

(defn write-response [resp]
  (->> resp
      response/compose
      :message
      (connection/write (:conn resp))))

(defn- log-request [{:keys [method path version logger] :as request}]
  (logging/log logger (str method " " path " " version))
  request)

(defn- attach-logger [request logger]
  (assoc request :logger logger))

(defn http [conn {:keys [entrypoint logger] :as app}]
    (-> conn
        create
        (attach-logger logger)
        log-request
        entrypoint
        write-response))
