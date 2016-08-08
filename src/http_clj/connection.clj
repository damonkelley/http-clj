(ns http-clj.connection
  (:require [clojure.java.io :as io])
  (:import java.net.Socket))

(defprotocol SocketConnection
  (readline [conn])
  (write [conn output])
  (close [conn]))

(defrecord Connection [socket reader writer]
  SocketConnection

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
  ([socket]
   (map->Connection {:socket socket
                     :reader (io/reader socket)
                     :writer (io/writer socket)}))
  ([host port]
   (let [socket (Socket. host port)]
     (map->Connection {:socket socket
                       :reader (io/reader socket)
                       :writer (io/writer socket)}))))
