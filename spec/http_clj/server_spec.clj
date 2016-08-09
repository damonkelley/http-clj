(ns http-clj.server-spec
  (:require [speclj.core :refer :all]
            [http-clj.server :refer [accept
                                     create
                                     listen
                                     AcceptingServer]]
            [http-clj.connection :as connection]
            [http-clj.mock :as mock]
            [com.stuartsierra.component :as component])
  (:import java.io.ByteArrayOutputStream))

(describe "a server component"
  (it "can be created using a port"
    (let [server (create 5001)]
      (should-be-a java.net.ServerSocket (:server-socket server))
      (component/stop server)))

  (it "can be created by injecting a ServerSocket"
    (let [server (create (mock/server))]
      (should-be-a java.net.ServerSocket (:server-socket server))))

  (it "will close the server"
    (let [server-socket (mock/server)
          server (create server-socket)]
      (should= false (.isClosed server-socket))
      (component/stop server)
      (should= true (.isClosed server-socket))))

  (it "will accept connections"
    (should= true (satisfies? connection/Connection
                              (-> (mock/server)
                                  (create)
                                  (accept))))))

(defrecord MockConnection [open]
  connection/Connection
  (readline [conn] "")

  (write [conn output] conn)

  (close [conn]
    (assoc conn :open false)))

(defrecord MockServer []
  AcceptingServer
  (accept [server]
    (MockConnection. true)))

(defn test-app [conn]
  (when (not (:open conn))
    (should-fail "The connection should be open"))
  (assoc conn :app-called true))

(describe "listen"
  (it "opens and closes connection"
    (should= false (:open (listen (MockServer.) identity))))
  (it "the application is sandwiched between opening and closing the connection"
    (should= true (:app-called (listen (MockServer.) test-app)))))
