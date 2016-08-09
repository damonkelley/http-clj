(ns http-clj.server
  (:require [com.stuartsierra.component :as component]
            [http-clj.connection :as connection])
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
    (connection/create (.accept server-socket))))

(defmulti create type)

(defmethod create Number
  [port]
  (map->Server {:server-socket (ServerSocket. port)}))

(defmethod create ServerSocket
  [server-socket]
  (map->Server {:server-socket server-socket}))

(defn listen [server app]
  (-> (accept server)
      (app)
      (connection/close)))

(defn- listen-until-interrupt [server app]
  (loop []
    (if (Thread/interrupted)
      server
      (do (listen server app) (recur)))))

(defn run [app port]
  (-> (create port)
      (component/start)
      (listen-until-interrupt app)
      (component/stop)))
