(ns http-clj.application.echo-spec
  (:require [speclj.core :refer :all]
            [http-clj.application.echo :refer [echo-loop]]
            [http-clj.connection :as connection]
            [http-clj.mock :as mock]
            [http-clj.spec.integration :refer [new-latch start-server shutdown-server]])
  (:import java.io.ByteArrayOutputStream))

(def test-port 5000)
(def test-host "localhost")

(defn pass-through-blocking-listener []
  (try
    (-> (connection/create test-host test-port)
        (connection/write "bye.\n")
        (connection/close))
    (catch java.net.ConnectException e nil)))

(describe "the echo loop"
  (it "echos until it receives bye."
    (let [output (ByteArrayOutputStream.)]
      (echo-loop (connection/create (mock/socket "foo\nbye." output)))
      (should= "foo\nGoodbye\n" (.toString output)))))

(describe "the echo-server"
  (around [it]
    (let [latch (new-latch)
          thread (start-server echo-loop test-port latch)]
      (.await latch)
      (it)
      (shutdown-server thread pass-through-blocking-listener)))

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
      (connection/close client2))))
