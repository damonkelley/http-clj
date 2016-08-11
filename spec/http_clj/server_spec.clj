(ns http-clj.server-spec
  (:require [speclj.core :refer :all]
            [http-clj.server :as s]
            [http-clj.connection :as connection]
            [http-clj.mock :as mock]
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

(defn test-app [conn]
  (when (not (:open conn))
    (should-fail "The connection should be open"))
  (assoc conn :app-called true))

(describe "listen"
  (it "opens and closes connection"
    (should= false (:open (s/listen (mock/server) identity))))
  (it "the application is sandwiched between opening and closing the connection"
    (should= true (:app-called (s/listen (mock/server) test-app)))))

(defn interrupting-app [conn]
  (loop [count 3]
    (cond
      (zero? count) (.interrupt (Thread/currentThread))
      (neg? count) (should-fail "The loop should exit after interrupt")
      :else (recur (dec count))))
  conn)

(describe "serve"
  (with server (mock/server))
  (it "starts the component"
    (should= true (:started (s/serve @server interrupting-app))))
  (it "listens until interrupted"
    (s/serve @server interrupting-app))
  (it "stops the component"
    (should= true (:stopped (s/serve @server interrupting-app)))))
