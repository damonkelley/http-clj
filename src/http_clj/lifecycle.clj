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

(defn- log-request [{:keys [method path version] :as request} logger]
  (logging/log logger :info (str method " " path " " version))
  request)

(defn validate-request [{:keys [method path] :as request}]
  (if (some empty? [method path])
    (assoc request :valid? false)
    (assoc request :valid? true)))

(defn- guard [request entrypoint]
  (if (:valid? request)
    (entrypoint request)
    (response/create request "" :status 400)))

(defn http [conn {:keys [entrypoint logger]}]
    (-> conn
        create
        (log-request logger)
        validate-request
        (guard entrypoint)
        write-response))
