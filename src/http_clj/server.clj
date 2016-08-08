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
    (.accept server-socket)))

(defmulti create type)

(defmethod create Number
  [port]
  (map->Server {:server-socket (ServerSocket. port)}))

(defmethod create ServerSocket
  [server-socket]
  (map->Server {:server-socket server-socket}))


(def exit-signal "bye.")
(def exit-message "Goodbye")

(defn- echo [text conn]
  (connection/write conn (str text \newline))
  conn)

(defn echo-loop [conn]
  (let [text (connection/readline conn)]
    (if (= exit-signal text)
      (echo exit-message conn)
      (recur (echo text conn)))))

(defn- listen [socket-server]
  (-> socket-server
      (accept)
      (connection/create)
      (echo-loop)
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
