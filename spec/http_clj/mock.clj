(ns http-clj.mock
  (:require [http-clj.connection :as connection]
            [http-clj.server :as server]
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

(defrecord MockConnection [open]
  connection/Connection
  (readline [conn] "")

  (write [conn output] conn)

  (close [conn]
    (assoc conn :open false)))

(defn connection []
  (MockConnection. true))

(defrecord MockServer [started stopped]
  component/Lifecycle
  (start [server]
    (assoc server :started true))

  (stop [server]
    (-> server
        (assoc :stopped true)))

  server/AcceptingServer
  (accept [server]
    (MockConnection. true)))

(defn server []
  (MockServer. false false))

