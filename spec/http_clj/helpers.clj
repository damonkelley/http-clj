(ns http-clj.helpers
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))


(def socket-connected? (atom true))

(defn mock-socket [input output]
  (reset! socket-connected? true)
  (proxy [java.net.Socket] []
    (close []
      (reset! socket-connected? false))

    (isClosed []
      (not @socket-connected?))

    (getOutputStream []
      output)

    (getInputStream []
      (ByteArrayInputStream.
        (.getBytes input)))))


(def is-open (atom true))

(defn mock-server []
  (proxy [java.net.ServerSocket] []
    (accept []
      (mock-socket "" (ByteArrayOutputStream.)))

    (close []
      false)))
