(ns http-clj.core
  (:require [http-clj.connection :as connection]
            [http-clj.server :as server]
            [com.stuartsierra.component :as component])
  (:import java.net.ServerSocket))


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
      (server/accept)
      (connection/create)
      (echo-loop)
      (connection/close))
  socket-server)

(defn- listen-until-interrupt [socket-server]
  (if (Thread/interrupted)
    socket-server
    (recur (listen socket-server))))

(defn -main [port & args]
  (-> (server/create port)
      (component/start)
      (listen-until-interrupt)
      (component/stop)))
