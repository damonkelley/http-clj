(ns http-clj.server
  (:require [com.stuartsierra.component :as component]))

(defrecord Server [server-socket]
  component/Lifecycle

  (start [component]
    component)

  (stop [component]
    (.close (:server-socket component))
    component))

(defn create [server-socket]
  (map->Server {:server-socket server-socket}))

(defn accept [component]
  (.accept (:server-socket component)))
