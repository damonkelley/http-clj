(ns http-clj.server-spec
  (:require [speclj.core :refer :all]
            [http-clj.server :as s]
            [http-clj.connection :as connection]
            [http-clj.response :as response]
            [http-clj.lifecycle :as lifecycle]
            [http-clj.spec-helper.mock :as mock]
            [com.stuartsierra.component :as component])
  (:import java.io.ByteArrayOutputStream))

(describe "a server component"
  (it "can be created using a port"
    (let [server (s/create 5001)]
      (should-be-a java.net.ServerSocket (:server-socket server))
      (component/stop server)))

  (it "can be created by injecting a ServerSocket"
    (let [server (s/create (mock/socket-server))]
      (should-be-a java.net.ServerSocket (:server-socket server))))

  (it "will close the server"
    (let [server-socket (mock/socket-server)
          server (s/create server-socket)]
      (should= false (.isClosed server-socket))
      (component/stop server)
      (should= true (.isClosed server-socket))))

  (it "will accept connections"
    (should= true (satisfies? connection/Connection
                              (-> (mock/socket-server)
                                  (s/create)
                                  (s/accept))))))

(defn test-app [request]
  (when (not (:open (:conn request)))
    (should-fail "The connection should be open"))
  (response/create request "App was called"))

(describe "listen"
  (with server (mock/server))
  (with application {:entrypoint test-app})
  (it "kicks off the request/response lifecycle"
    (should-invoke lifecycle/http {:times 1 :return (mock/connection)}
                   (s/listen @server @application)))

  (it "closes the connection"
    (let [{open :open} (s/listen @server @application)]
      (should= false open))))

(defn interrupting-app [request]
  (loop [count 3]
    (cond
      (zero? count) (.interrupt (Thread/currentThread))
      (neg? count) (should-fail "The loop should exit after interrupt")
      :else (recur (dec count))))
  (response/create request ""))

(describe "serve"
  (with server (mock/server))
  (with application {:entrypoint interrupting-app})
  (it "starts the component"
    (should= true (:started (s/serve @server @application))))

  (it "listens until interrupted"
    (s/serve @server @application))

  (it "stops the component"
    (should= true (:stopped (s/serve @server @application)))))
