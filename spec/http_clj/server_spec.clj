(ns http-clj.server-spec
  (:require [speclj.core :refer :all]
            [http-clj.server :refer [accept create]]
            [http-clj.helpers :refer [mock-server]]
            [com.stuartsierra.component :as component]))


(describe "a server component"
  (with server (create (mock-server)))

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
