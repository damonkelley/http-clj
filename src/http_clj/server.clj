(ns http-clj.server
  (:require [com.stuartsierra.component :as component]
            [http-clj.connection :as connection]
            [http-clj.echo :as echo])
  (:import java.net.ServerSocket))

(defprotocol AcceptingServer
  (accept [component]))

(defrecord Server [server-socket]
  component/Lifecycle
  (start [component]
    component)

  (stop [component]
    (.close server-socket)
    component)

  AcceptingServer
  (accept [component]
    (.accept server-socket)))

(defmulti create type)

(defmethod create Number
  [port]
  (map->Server {:server-socket (ServerSocket. port)}))

(defmethod create ServerSocket
  [server-socket]
  (map->Server {:server-socket server-socket}))

(defn- listen [socket-server]
  (-> socket-server
      (accept)
      (connection/create)
      (echo/echo-loop)
      (connection/close))
  socket-server)

(defn- listen-until-interrupt [socket-server]
  (if (Thread/interrupted)
    socket-server
    (recur (listen socket-server))))

(defn -main [port & args]
  (-> (create port)
      (component/start)
      (listen-until-interrupt)
      (component/stop)))
