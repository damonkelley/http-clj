(ns http-clj.server-spec
  (:require [speclj.core :refer :all]
            [http-clj.server :refer [accept create]]
            [http-clj.spec-helpers :refer [mock-server]]
            [com.stuartsierra.component :as component]))


(describe "a server component"
  (with server (create (mock-server)))

  (it "can be created using a port"
    (let [server (create 5001)]
      (should-be-a java.net.ServerSocket (:server-socket server))
      (component/stop server)))

  (it "can be created by injecting a ServerSocket"
    (let [server (create (mock-server))]
      (should-be-a java.net.ServerSocket (:server-socket server))))

  (it "will close the server"
    (let [server-socket (mock-server)
          server (create server-socket)]
      (should= false (.isClosed server-socket))
      (component/stop server)
      (should= true (.isClosed server-socket))))

  (it "will accept connections"
    (should-be-a java.net.Socket
                 (-> (mock-server)
                     (create)
                     (accept)))))
