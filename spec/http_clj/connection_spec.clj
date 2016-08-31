(ns http-clj.connection-spec
  (:require [speclj.core :refer :all]
            [http-clj.connection :as connection]
            [http-clj.spec-helper.mock :as mock])
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))

(def output
  (ByteArrayOutputStream.))

(describe "a connection"
  (with conn (connection/create (mock/socket "line 1\nline 2" output)))

  (it "reads one character"
    (should= (int \l) (connection/read-char @conn))
    (should= (int \i) (connection/read-char @conn)))

  (it "reads the input as a byte array"
    (let [length (alength (.getBytes "line 1\nline 2"))]
      (should= "line 1\nline 2" (String. (connection/read-bytes @conn length)))))

  (it "will yield a connection when written to"
    (should= @conn (connection/write @conn (.getBytes ""))))

  (it "can be written to"
    (connection/write @conn (.getBytes "data written to out"))
    (should= "data written to out" (.toString output)))

  (it "can be closed"
    (should= false (.isClosed (:socket @conn)))
    (should= nil (:socket (connection/close @conn)))
    (should= true (.isClosed (:socket @conn)))))
