(ns http-clj.application.hello-world
  (:require [http-clj.response :as response]
            [http-clj.connection :as connection]
            [http-clj.server :refer [run]]))

(defn- readall [conn]
  (while (not= "" (connection/readline conn)) conn))

(defn app [conn]
  (readall conn)
  (->> (response/create "Hello, world!")
       (response/compose)
       (connection/write conn)))

(defn -main [& args]
  (run app 5000))
