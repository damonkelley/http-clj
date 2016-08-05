(ns http-clj.helpers
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))


(defn mock-socket [input output]
  (let [connected? (atom true)]
   (proxy [java.net.Socket] []
     (close []
       (reset! connected? false))

     (isClosed []
       (not @connected?))

     (getOutputStream []
       output)

     (getInputStream []
       (ByteArrayInputStream.
         (.getBytes input))))))


(def is-open (atom true))

(defn mock-server []
  (proxy [java.net.ServerSocket] []
    (accept []
      (mock-socket "" (ByteArrayOutputStream.)))

    (close []
      false)))
