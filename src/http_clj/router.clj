(ns http-clj.router
  (:require [http-clj.response :as response]
            [http-clj.request-handler :as handler]))

(defn route [request routes & {:keys [fallback]
                               :or {fallback handler/not-found}}]
  (let [{method :method path :path} request
        handler (get routes [method path] fallback)]
    (handler request)))
