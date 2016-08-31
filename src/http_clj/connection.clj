(ns http-clj.connection
  (:require [clojure.java.io :as io])
  (:import java.net.Socket
           java.io.InputStreamReader))

(defprotocol Connection
  (read-bytes [conn buffer])
  (read-byte [conn])
  (write [conn output])
  (close [conn]))

(defrecord SocketConnection [socket]
  Connection
  (read-byte [conn]
    (.read (.getInputStream socket)))

  (read-bytes [conn length]
    (let [buffer (byte-array length)]
      (doto (.getInputStream socket)
        (.read buffer))
      buffer))

  (write [conn output]
    (doto (.getOutputStream socket)
      (.write output)
      (.flush))
    conn)

  (close [conn]
    (.close socket)
    (assoc conn :socket nil)))

(defn create
  ([host port] (create (Socket. host port)))
  ([socket]
   (map->SocketConnection {:socket socket})))
