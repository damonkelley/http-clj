(ns http-clj.connection-spec
  (:require [speclj.core :refer :all]
            [http-clj.connection :refer :all]))

(import java.io.ByteArrayInputStream
        java.io.ByteArrayOutputStream
        java.io.BufferedReader
        java.io.PrintWriter)

(def output
  (ByteArrayOutputStream.))

(def mock-socket
  (proxy [java.net.Socket] []
    (getOutputStream [] output)
    (getInputStream []
      (ByteArrayInputStream.
        (.getBytes "mock connection input data\nmore data from input")))))

(describe "a connection"
          (with conn (new-connection mock-socket))
          (it "is constructed from a socket"
              (should= mock-socket (:socket @conn))
              (should-be-a BufferedReader (:reader @conn))
              (should-be-a PrintWriter (:writer @conn)))
          (it "can be read from"
              (should= "mock connection input data" (readline @conn))
              (should= "more data from input" (readline @conn)))
          (it "can be written to"
              (write @conn "data written to out")
              (should= "data written to out\n" (.toString output))))
