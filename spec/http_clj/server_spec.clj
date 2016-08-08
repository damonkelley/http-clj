(ns http-clj.server-spec
  (:require [speclj.core :refer :all]
            [http-clj.server :refer [accept create]]
            [http-clj.connection :as connection]
            [http-clj.spec-helpers :refer [mock-server mock-socket]]
            [com.stuartsierra.component :as component])
  (:import java.io.ByteArrayOutputStream))

(def test-port 5000)

(def test-host "localhost")

(defn start-server []
  (let [thread (Thread. #(http-clj.core/-main test-port))]
    (.start thread)
    thread))

(defn pass-through-blocking-listener []
  (try
    (-> (connection/create test-host test-port)
        (connection/write "bye.\n")
        (connection/close))
    (catch java.net.ConnectException e nil)))

(defn shutdown-server [thread]
  (.interrupt thread)
  (pass-through-blocking-listener))

(defn warmup []
  (Thread/sleep 100))

(describe "a server"
  (context "as a component"
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

  (context "as an echo-server"
    (around [it]
      (let [thread (start-server)]
        (warmup)
        (it)
        (shutdown-server thread)))

    (it "echos back input and closes"
      (let [client (connection/create test-host test-port)]

        (connection/write client (str "foo" \newline
                                      "bye." \newline))

        (should= "foo" (connection/readline client))
        (should= "Goodbye" (connection/readline client))
        (connection/close client)))

    (it "accepts connections in serial"
      (let [client1 (connection/create test-host test-port)
            client2 (connection/create test-host test-port)]

        (connection/write client1 (str "foo" \newline
                                       "bye." \newline))

        (connection/write client2 (str "foo" \newline
                                       "bar" \newline
                                       "bye." \newline))

        (should= "foo" (connection/readline client1))
        (should= "Goodbye" (connection/readline client1))
        (connection/close client1)

        (should= "foo" (connection/readline client2))
        (should= "bar" (connection/readline client2))
        (should= "Goodbye" (connection/readline client2))
        (connection/close client2)))))
