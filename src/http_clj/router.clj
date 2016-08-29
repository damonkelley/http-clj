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

(defn- route-matches? [{method :method path :path} route]
  (and (= (first route) method)
       (path-matches? path (second route))))

(defn- find-route [request routes]
  (first (filter (partial route-matches? request) routes)))

(defn- has-route-for-path [{request-path :path} routes]
  (some #(path-matches? request-path (second %)) routes))

(defn choose-handler [request routes]
  (let [matching-route (find-route request routes)]
    (cond matching-route (last matching-route)
          (has-route-for-path request routes) handler/method-not-allowed
          :else handler/not-found)))

(defn route [request routes]
  (let [handler (choose-handler request routes)]
    (handler request)))
