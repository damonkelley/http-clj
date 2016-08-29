(ns http-clj.router
  (:require [http-clj.request-handler :as handler]))

(defmulti path-matches?
  (fn [_ route-path]
    (type route-path)))

(defmethod path-matches? String
  [request-path route-path]
  (= request-path route-path))

(defmethod path-matches? java.util.regex.Pattern
  [request-path route-path]
  (not (nil? (re-matches route-path request-path))))

(defn- find-route [{path :path} routes]
  (first (filter #(path-matches? path (:path %)) routes)))

(defn choose-handler [{:keys [method] :as request} routes]
  (if-let [route (find-route request routes)]
    (get-in route [:handlers method] handler/method-not-allowed)
    handler/not-found))

(defn route [request routes]
  (let [handler (choose-handler request routes)]
    (handler request)))
