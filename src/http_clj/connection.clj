(ns http-clj.connection)

(import java.io.InputStreamReader
        java.io.BufferedReader
        java.io.PrintWriter)

(defrecord Connection [connection reader])

(defn new-connection [socket]
  (map->Connection {:socket socket
                    :reader (BufferedReader. (InputStreamReader. (.getInputStream socket)))
                    :writer (PrintWriter. (.getOutputStream socket) true)}))

(defn readline [connection]
  (.readLine (:reader connection)))

(defn write [connection output]
  (.println (:writer connection) output))
