(ns http-clj.core-spec
  (:require [speclj.core :refer :all]
            [http-clj.core :refer :all]
            [http-clj.connection :as connection]
            [http-clj.spec-helpers :refer [mock-socket]])
  (:import java.io.ByteArrayOutputStream))


(def test-port 5000)
(def test-host "localhost")


(defn start-server []
  (let [thread (Thread. #(http-clj.core/-main test-port))]
    (.start thread)
    thread))


(defn pass-through-blocking-listener []
  (try
    (connection/close
      (connection/write
        (connection/create test-host test-port)
        "bye.\n"))
    (catch java.net.ConnectException e nil)))


(defn shutdown-server [thread]
  (.interrupt thread)
  (pass-through-blocking-listener))


(defn warmup []
  (Thread/sleep 100))


(describe "an echo-server"
  (context "the echo loop"
    (with output (ByteArrayOutputStream.))

    (it "echos until it receives bye."
      (echo-loop (connection/create (mock-socket "foo\nbye." @output)))
      (should= "foo\nGoodbye\n" (.toString @output))))

  (context "the server"
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
