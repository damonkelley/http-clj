(ns http-clj.app.cob-spec.handlers-spec
  (:require [speclj.core :refer :all]
            [http-clj.app.cob-spec.handlers :refer :all]
            [clojure.java.io :as io])
  (:import java.io.ByteArrayOutputStream))

(describe "handlers"
  (context "static"
    (with dir "resources/static/")
    (it "responds with a listing if directory"
      (should-contain "image.gif" (:body (static {:path "/"} @dir))))

    (it "responds with a file if it is a file"
      (should-contain "File contents" (String. (:body (static {:path "/file.txt"} @dir)))))

    (it "responds with 404 if a file is not found"
      (should= 404 (:status (static {:path "/file-that-does-not-exist.txt"} @dir)))))

  (context "log"
    (with output (ByteArrayOutputStream.))
    (with request-log (io/writer @output))

    (it "displays the contents of the log"
        (.write @request-log "message 1\n")
        (.flush @request-log)
        (should-contain "message 1\n" (:body (log {} @output))))))
