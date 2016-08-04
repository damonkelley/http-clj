(ns http-clj.connection-spec
  (:require [speclj.core :refer :all]
            [http-clj.connection :refer [write readline new-connection]]
            [http-clj.helpers :refer [mock-socket]])
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))


(def output
  (ByteArrayOutputStream.))


(describe "a connection"
  (with conn (new-connection (mock-socket "line 1\nline 2" output)))
  (it "can be read from"
    (should= "line 1" (readline @conn))
    (should= "line 2" (readline @conn)))
  (it "can be written to"
    (write @conn "data written to out")
    (should= "data written to out\n" (.toString output))))
