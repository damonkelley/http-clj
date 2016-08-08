(ns http-clj.echo-spec
  (:require [speclj.core :refer :all]
            [http-clj.echo :refer [echo-loop]]
            [http-clj.connection :as connection]
            [http-clj.mock :as mock])
  (:import java.io.ByteArrayOutputStream))

(describe "the echo loop"
  (it "echos until it receives bye."
    (let [output (ByteArrayOutputStream.)]
      (echo-loop (connection/create (mock/socket "foo\nbye." output)))
      (should= "foo\nGoodbye\n" (.toString output)))))
