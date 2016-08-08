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

(defmulti create type)

(defmethod create Number
  [port]
  (map->Server {:server-socket (ServerSocket. port)}))

(defmethod create ServerSocket
  [server-socket]
  (map->Server {:server-socket server-socket}))
