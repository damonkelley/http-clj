(ns http-clj.connection
  (:require [clojure.java.io :as io])
  (:import java.net.Socket))

(defprotocol Connection
  (readline [conn])
  (write [conn output])
  (close [conn]))

(defrecord SocketConnection [socket reader writer]
  Connection

  (readline [conn]
    (.readLine reader))

  (write [conn output]
    (.write writer output)
    (.flush writer)
    conn)

  (close [conn]
    (.close socket)
    (assoc conn :socket nil)))

(defn create
  ([host port] (create (Socket. host port)))
  ([socket]
   (map->SocketConnection {:socket socket
                           :reader (io/reader socket)
                           :writer (.getOutputStream socket)})))
