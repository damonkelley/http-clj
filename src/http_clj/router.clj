(ns http-clj.router
  (:require [http-clj.response :as response]
            [http-clj.request-handler :as handler]))

(defn route [request routes]
  (let [{method :method path :path} request
        handler (get routes [method path] handler/not-found)]
    (handler request)))
