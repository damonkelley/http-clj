(ns http-clj.mock
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))

(defn socket [input output]
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

(defn server []
  (let [closed? (atom false)]
    (proxy [java.net.ServerSocket] []
      (accept []
        (socket "" (ByteArrayOutputStream.)))

      (close []
        (reset! closed? true))

      (isClosed []
        @closed?))))
