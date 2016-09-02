(ns http-clj.server-spec
  (:require [speclj.core :refer :all]
            [http-clj.server :as s]
            [http-clj.connection :as connection]
            [http-clj.response :as response]
            [http-clj.protocol :as protocol]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.logging :as logging]
            [com.stuartsierra.component :as component])
  (:import java.io.ByteArrayOutputStream))

(describe "a server component"
  (with application {:entrypoint identity})
  (it "can be created using a port"
    (let [server (s/create @application :port 5001)]
      (should-be-a java.net.ServerSocket (:server-socket server))
      (component/stop server)))

  (it "can be created by injecting a ServerSocket"
    (let [server (s/create @application :server-socket mock/socket-server)]
      (should-be-a java.net.ServerSocket (:server-socket server))))

  (it "will close the server"
    (let [server-socket (mock/socket-server)
          server (s/create @application :server-socket mock/socket-server)]
      (should= false (.isClosed (:server-socket server)))
      (component/stop server)
      (should= true (.isClosed (:server-socket server)))))

  (it "will accept connections"
    (should= true (satisfies? connection/Connection
                              (-> (s/create @application :server-socket mock/socket-server)
                                  (s/accept))))))

(defrecord DegenerateLogger []
  logging/Logger
  (log [this level contents]))

(defn test-app [request]
  (when (.isClosed (:socket (:conn request)))
    (should-fail "The connection should be open"))
  (response/create request "App was called"))

(describe "listen"
  (with server (mock/server))
  (with application {:entrypoint test-app
                     :logger (->DegenerateLogger)})
  (it "dispatches to http"
    (should-invoke protocol/http {:times 1 :return (mock/connection)}
                   (s/listen @server @application)))

  (it "closes the connection"
    (let [{socket :socket} (s/listen @server @application)]
      (should= nil socket))))

(defn interrupting-app [request]
  (loop [count 3]
    (cond
      (zero? count) (.interrupt (Thread/currentThread))
      (neg? count) (should-fail "The loop should exit after interrupt")
      :else (recur (dec count))))
  (response/create request ""))

(describe "serve"
  (with server (mock/server))
  (with application {:entrypoint interrupting-app
                     :logger (->DegenerateLogger)})
  (it "starts the component"
    (should= true (:started (s/serve @server))))

  (it "stops the component"
    (should= true (:stopped (s/serve @server)))))
