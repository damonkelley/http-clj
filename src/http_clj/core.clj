(ns http-clj.core
  (:require [http-clj.connection :as connection]
            [http-clj.server :as server]
            [com.stuartsierra.component :as component])
  (:import java.net.ServerSocket))


(defn- echo [text conn]
  (connection/write conn (str text \newline))
  conn)

(defn echo-loop [conn]
  (loop [conn conn]
    (let [text (connection/readline conn)]
      (if (= "bye." text)
        (echo "Goodbye" conn)
        (recur (echo text conn))))))

(defn- listen [socket-server]
  (-> socket-server
      (server/accept)
      (connection/new-connection)
      (echo-loop)
      (connection/close))
  socket-server)

(defn- listen-until-interrupt [socket-server]
  (if (Thread/interrupted)
    socket-server
    (recur (listen socket-server))))

(defn -main [& args]
  (-> (server/new-server (ServerSocket. 5000))
      (component/start)
      (listen-until-interrupt)
      (component/stop)))
