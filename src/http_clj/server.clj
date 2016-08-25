(ns http-clj.server
  (:require [com.stuartsierra.component :as component]
            [http-clj.connection :as connection]
            [http-clj.lifecycle :as lifecycle]
            [http-clj.logging :as logging])
  (:import java.net.ServerSocket
           java.util.concurrent.CountDownLatch))

(defprotocol Server
  (accept [component]))

(defn listen [server app]
  (-> server
      accept
      (lifecycle/http app)
      connection/close))

(defn- open-latch [latch]
  (.countDown latch))

(defn- listen-until-interrupt [server app latch]
  (while (not (Thread/interrupted))
    (open-latch latch)
    (listen server app))
  server)

(defrecord ConnectionServer [server-socket application latch]
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

(defn create [& {:keys [port server-socket latch application]
                 :or {port 5000
                      server-socket #(ServerSocket. %)
                      latch (CountDownLatch. 0)
                      application identity}}]
  (map->ConnectionServer
    {:server-socket (server-socket port)
     :application application
     :latch latch}))

(defn serve [server]
  (-> server
      (component/start)
      (component/stop)))

(defn run [app port]
  (serve (create :port port :application app)))
