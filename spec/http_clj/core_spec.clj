(ns http-clj.core-spec
 (:require [speclj.core :refer :all]
       [http-clj.core :refer :all]
       [http-clj.connection :refer [new-connection]]
       [http-clj.helpers :refer [mock-socket]])
 (:import java.io.ByteArrayOutputStream))


(describe "An echo loop"
  (with output (ByteArrayOutputStream.))
  (it "echos until it receives bye."
    (echo-loop (new-connection (mock-socket "foo\nbye." @output)))
    (should= "foo\nGoodbye\n" (.toString @output))))
