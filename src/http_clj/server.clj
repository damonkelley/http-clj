(ns http-clj.server
  (:require [com.stuartsierra.component :as component]
            [http-clj.connection :as connection]
            [http-clj.protocol :as protocol]
            [http-clj.logging :as logging])
  (:import java.net.ServerSocket
           java.util.concurrent.CountDownLatch
           java.util.concurrent.Executors))

(defprotocol Server
  (accept [component]))

(defn process-request [conn app]
  (-> conn
      (protocol/http app)
      connection/close))

(defn new-worker [thread-pool app]
  (.execute thread-pool app))

(defn listen [server app]
  (let [conn (accept server)]
    (new-worker (:thread-pool server) #(process-request conn app))))

(defn- open-latch [latch]
  (.countDown latch))

(defn- listen-until-interrupt [server app latch]
  (while (not (Thread/interrupted))
    (open-latch latch)
    (listen server app))
  server)

(defrecord ConnectionServer [server-socket application thread-pool latch]
  component/Lifecycle
  (start [server]
    (listen-until-interrupt server application latch)
    server)

  (stop [component]
    (.close server-socket)
    component)

  Server
  (accept [component]
    (-> server-socket
        .accept
        connection/create)))

(defn create [application & {:keys [port server-socket thread-pool latch ]
                             :or {port 5000
                                  server-socket #(ServerSocket. %)
                                  thread-pool (Executors/newSingleThreadExecutor)
                                  latch (CountDownLatch. 0)}}]
  (->ConnectionServer (server-socket port) application thread-pool latch))

(defn serve [server]
  (-> server
      (component/start)
      (component/stop)))

(defn run [app port thread-pool]
  (serve (create app :port port :thread-pool thread-pool)))
