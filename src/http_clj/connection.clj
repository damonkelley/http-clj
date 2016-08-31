(ns http-clj.connection
  (:require [clojure.java.io :as io])
  (:import java.net.Socket
           java.io.InputStreamReader))

(defprotocol Connection
  (read-bytes [conn buffer])
  (read-char [conn])
  (write [conn output])
  (close [conn]))

(defrecord SocketConnection [socket reader writer]
  Connection
  (read-char [conn]
    (.read reader))

  (read-bytes [conn length]
    (let [buffer (byte-array length)]
      (.read (.getInputStream socket) buffer)
      buffer))

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
                           :reader (InputStreamReader. (.getInputStream socket))
                           :writer (.getOutputStream socket)})))
