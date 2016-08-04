(ns http-clj.helpers
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))


(def ^:private is-open
  (atom true))

(defn mock-socket [input output]
  (proxy [java.net.Socket] []
    (getOutputStream [] output)
    (getInputStream []
      (ByteArrayInputStream.
        (.getBytes input)))))


(defn mock-server []
  (proxy [java.net.ServerSocket] []
    (accept []
      (mock-socket "" (ByteArrayOutputStream.)))
    (close []false)))
