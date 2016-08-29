(ns http-clj.router
  (:require [http-clj.response :as response]
            [http-clj.request-handler :as handler]))

(defmulti path-matches?
  (fn [request-path route-path]
    (type route-path)))

(defmethod path-matches? String
  [request-path route-path]
  (= request-path route-path))

(defmethod path-matches? java.util.regex.Pattern
  [request-path route-path]
  (not (nil? (re-matches route-path request-path))))

(defn- find-route [{method :method path :path} routes]
  (first
    (filter
      #(and (= (first %1) method) (path-matches? path (second %1)))
      routes)))

(defn route [request routes & {:keys [fallback]
                               :or {fallback handler/not-found}}]
  (let [fallback-route [nil nil fallback]
        [_ _ handler] (or (find-route request routes) fallback-route)]
    (handler request)))
