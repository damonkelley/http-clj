(ns http-clj.server
  (:require [com.stuartsierra.component :as component]))

(defrecord Server [server-socket]
  component/Lifecycle
  (stop [component]
    (.close (:server-socket component))
    component))

(defn new-server [server-socket]
  (map->Server {:server-socket server-socket}))

(defn accept [component]
  (.accept (:server-socket component)))
