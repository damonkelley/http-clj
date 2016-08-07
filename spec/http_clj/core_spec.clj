(ns http-clj.core-spec
  (:require [speclj.core :refer :all]
            [http-clj.core :refer :all]
            [http-clj.connection :as connection]
            [http-clj.helpers :refer [mock-socket]])
  (:import java.io.ByteArrayOutputStream
           java.net.Socket))


(defn start-server []
  (let [thread (Thread. http-clj.core/-main)]
    (.start thread)
    thread))


(defn pass-through-blocking-accept []
  (try
    (connection/close
      (connection/write
        (connection/new-connection (Socket. "localhost" 5000))
        "bye.\n"))
    (catch java.net.ConnectException e nil)))


(defn shutdown-server [thread]
  (.interrupt thread)
  (pass-through-blocking-accept))


(describe "an echo-server"
  (context "the echo loop"
    (with output (ByteArrayOutputStream.))

    (it "echos until it receives bye."
      (echo-loop (connection/new-connection (mock-socket "foo\nbye." @output)))
      (should= "foo\nGoodbye\n" (.toString @output))))

  (context "the server"
    (around [it]
      (let [thread (start-server)]
        (it)
        (shutdown-server thread)))

    (it "echos back input and closes"
      (let [client (connection/new-connection (Socket. "localhost" 5000))]

        (connection/write client (str "foo" \newline
                                      "bye." \newline))

        (should= "foo" (connection/readline client))
        (should= "Goodbye" (connection/readline client))
        (connection/close client)))

    (it "accepts connections in serial"
      (let [client1 (connection/new-connection (Socket. "localhost" 5000))
            client2 (connection/new-connection (Socket. "localhost" 5000))]

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
