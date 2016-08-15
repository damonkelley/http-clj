(ns http-clj.application.hello-world
  (:require [http-clj.response :as response]
            [http-clj.server :refer [run]]))

(defn app [request]
  (response/create request "Hello, world!"))

(defn -main [& args]
  (run app 5000))
