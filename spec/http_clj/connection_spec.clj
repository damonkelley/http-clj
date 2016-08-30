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

  (it "can be have lines read from it"
    (should= "line 1" (connection/readline @conn))
    (should= "line 2" (connection/readline @conn)))

  (it "can be read into a buffer"
    (let [buffer-length (alength (.getBytes "line 1\nline 2"))
          buffer (connection/read @conn (byte-array buffer-length))]
      (should= "line 1\nline 2" (String. buffer))))

  (it "will yield a connection when written to"
    (should= @conn (connection/write @conn (.getBytes ""))))

  (it "can be written to"
    (connection/write @conn (.getBytes "data written to out"))
    (should= "data written to out" (.toString output)))

  (it "can be closed"
    (should= false (.isClosed (:socket @conn)))
    (should= nil (:socket (connection/close @conn)))
    (should= true (.isClosed (:socket @conn)))))
