(ns http-clj.router
  (:require [http-clj.response :as response]))

(defn- route-not-found [request]
  (response/create request "Route not found" :status 404))

(defn route [request routes]
  (let [{method :method path :path} request
        handler (get routes [method path] route-not-found)]
    (handler request)))
