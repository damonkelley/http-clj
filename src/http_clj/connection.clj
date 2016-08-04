(ns http-clj.connection
  (:require [clojure.java.io :as io]))

(defrecord Connection [connection reader])

(defn new-connection [socket]
  (map->Connection {:socket socket
                    :reader (io/reader socket)
                    :writer (io/writer socket)}))

(defn readline [connection]
  (.readLine (:reader connection)))

(defn write [connection output]
  (.write (:writer connection) output)
  (.flush (:writer connection))
  connection)

(defn close [connection]
  (.close (:socket connection))
  (assoc connection :socket nil))
