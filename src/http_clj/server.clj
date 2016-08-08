(ns http-clj.server
  (:require [com.stuartsierra.component :as component])
  (:import java.net.ServerSocket))

(defprotocol AcceptingServer
  (accept [component]))

(defrecord Server [server-socket]
  component/Lifecycle
  AcceptingServer

  (start [component]
    component)

  (stop [component]
    (.close server-socket)
    component)

  (accept [component]
    (.accept server-socket)))

(defn create [server-socket]
  (map->Server {:server-socket server-socket}))
