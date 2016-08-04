(ns http-clj.connection-spec
  (:require [speclj.core :refer :all]
            [http-clj.connection :refer [write readline new-connection close]]
            [http-clj.helpers :as helpers])
  (:import java.io.ByteArrayInputStream
           java.io.ByteArrayOutputStream))


(def output
  (ByteArrayOutputStream.))


(describe "a connection"
  (with conn (new-connection (helpers/mock-socket "line 1\nline 2" output)))

  (it "can be read from"
    (should= "line 1" (readline @conn))
    (should= "line 2" (readline @conn)))

  (it "will yield a connection when written to"
   (should= @conn (write @conn "")))

  (it "can be written to"
    (write @conn "data written to out")
    (should= "data written to out" (.toString output)))


  (it "can be closed"
     (should= false (.isClosed (:socket @conn)))
     (should= nil (:socket (close @conn)))
     (should= true (.isClosed (:socket @conn)))))
