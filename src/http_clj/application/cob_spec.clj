(ns http-clj.application.cob-spec
  (:require [http-clj.router :refer [route]]
            [http-clj.response :as response]
            [http-clj.request-handler :as handler]
            [http-clj.server :refer [run]]))

(defn index [request]
  (response/create request "cob spec"))

(defn static [path]
  (str "src/http_clj/application/static/" path))

(defn app [request]
  (route
    request
    {["GET" "/"] index
     ["GET" "/image.gif"] (handler/file (static "image.gif"))}))

(defn -main [& args]
  (run app 5000))
