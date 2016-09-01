(ns http-clj.router
  (:require [http-clj.request-handler :as handler]
            [http-clj.router.route-helpers :as helper]))

(defn choose-handler [{:keys [path method] :as request} routes]
  (if-let [route (helper/find-route routes path)]
    (get-in route [:handlers method] handler/method-not-allowed)
    handler/not-found))

(defn route [request routes]
  (let [handler (choose-handler request routes)]
    (handler request)))

(defn GET
  ([routes path handler]
   (GET routes
        path
        handler
        (partial handler/head handler)))

  ([routes path get-handler head-handler]
   (helper/update-route
     routes
     {:path path
      :handlers {"GET" get-handler
                 "HEAD" head-handler}})))

(defn POST [routes path handler]
  (helper/update-route
    routes
    {:path path
     :handlers {"POST" handler}}))

(defn OPTIONS [routes path handler]
  (helper/update-route
    routes
    {:path path
     :handlers {"OPTIONS" handler}}))
