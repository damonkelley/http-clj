(ns http-clj.mock
  (:require [http-clj.connection :as connection]
            [http-clj.server :as server]
            [clojure.java.io :as io]
            [com.stuartsierra.component :as component])
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

(defn socket-server []
  (let [closed? (atom false)]
    (proxy [java.net.ServerSocket] []
      (accept []
        (socket "" (ByteArrayOutputStream.)))

      (close []
        (reset! closed? true))

      (isClosed []
        @closed?))))

(defrecord MockConnection [open input]
  connection/Connection
  (readline [conn]
    (.readLine input))

  (write [conn output] conn)

  (close [conn]
    (assoc conn :open false)))

(defn connection
  ([]
   (connection ""))
  ([input]
   (MockConnection. true (io/reader (.getBytes input)))))

(defrecord MockServer [started stopped]
  component/Lifecycle
  (start [server]
    (assoc server :started true))

  (stop [server]
    (-> server
        (assoc :stopped true)))

  server/AcceptingServer
  (accept [server]
    (connection)))

(defn server []
  (MockServer. false false))
