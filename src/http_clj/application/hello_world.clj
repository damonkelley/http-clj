(ns http-clj.application.hello-world
  (:require [http-clj.response :as response]
            [http-clj.request :as request]
            [http-clj.connection :as connection]
            [http-clj.server :refer [run]]))

(defn app [conn]
  (connection/write conn (-> (request/create conn)
                         (response/create "Hello, world!")
                         (response/compose)
                         (:message))))

(defn -main [& args]
  (run app 5000))
