(ns http-clj.protocol
  (:require [http-clj.request :as request]
            [http-clj.response :as response]
            [http-clj.connection :as connection]
            [http-clj.logging :as logging]))

(defn- log-request [{:keys [method path version] :as request} logger]
  (logging/log logger :info (str method " " path " " version))
  request)

(defn- guard [request entrypoint]
  (if (:valid? request)
    (entrypoint request)
    (response/create request "" :status 400)))

(defn http [conn {:keys [entrypoint logger]}]
    (-> conn
        request/create
        (log-request logger)
        (guard entrypoint)
        response/write))
