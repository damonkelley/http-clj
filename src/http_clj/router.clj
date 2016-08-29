(ns http-clj.router
  (:require [http-clj.response :as response]
            [http-clj.request-handler :as handler]))

(defn- find-route [{method :method path :path} routes]
  (first
    (filter
      #(and (= (first %1) method) (= (second %1) path))
      routes)))

(defn route [request routes & {:keys [fallback]
                               :or {fallback handler/not-found}}]
  (let [fallback-route [nil nil fallback]
        [_ _ handler] (or (find-route request routes) fallback-route)]
    (handler request)))
