(ns http-clj.spec-helper.mock
  (:require [http-clj.connection :as connection]
            [http-clj.server :as server]
            [com.stuartsierra.component :as component]
            [http-clj.spec-helper.request-generator :refer [GET]])
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))

(defn socket [input output]
  (let [connected? (atom true)
        input-stream (ByteArrayInputStream. (.getBytes input))]
    (proxy [java.net.Socket] []
      (close []
        (reset! connected? false))

      (isClosed []
        (not @connected?))

      (getOutputStream []
        output)

      (getInputStream []
        input-stream))))

(defn socket-server [& args]
  (let [closed? (atom false)]
    (proxy [java.net.ServerSocket] []
      (accept []
        (socket "" (ByteArrayOutputStream.)))

      (close []
        (reset! closed? true))

      (isClosed []
        @closed?))))

(defrecord MockConnection [open input-stream]
  connection/Connection
  (write [conn text]
    (assoc conn :written-to-connection (String. text)))

  (read-byte [conn]
    (.read input-stream))

  (read-bytes [conn length]
    (let [buffer (byte-array length)]
      (.read input-stream buffer)
      buffer))

  (close [conn]
    (assoc conn :open false)))

(defn connection
  ([]
   (connection ""))
  ([input]
   (MockConnection. true (ByteArrayInputStream. (.getBytes input)))))

(defrecord MockServer [started stopped]
  component/Lifecycle
  (start [server]
    (assoc server :started true))

  (stop [server]
    (-> server
        (assoc :stopped true)))

  server/Server
  (accept [server]
    (connection (GET "/" {"Host" "www.example.com"}))))

(defn server []
  (MockServer. false false))
