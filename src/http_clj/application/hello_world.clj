(ns http-clj.application.hello-world
  (:require [http-clj.response :as response]
            [http-clj.router :refer [route]]
            [http-clj.server :refer [run]]))

(defn hello-world [request]
  (response/create request "Hello, world!"))

(defn foo [request]
  (response/create request "Bar"))

(defn app [request]
  (route
    request
    {["GET" "/"] hello-world
     ["GET" "/foo"] foo}))

(defn -main [& args]
  (run {:entrypoint app} 5000))
