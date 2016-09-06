(ns http-clj.app.cob-spec.handlers-spec
  (:require [speclj.core :refer :all]
            [http-clj.app.cob-spec.handlers :refer :all]
            [clojure.java.io :as io]
            [clojure.data.codec.base64 :as b64])
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

    (it "displays the log"
      (.write @request-log "message 1\n")
      (.flush @request-log)
      (should= 200 (:status (log {} @output)))
      (should-contain "message 1\n" (:body (log {} @output)))))

  (context "form"
    (context "submit-form"
      (it "updates the form"
        (let [cache (atom "")]
          (should= 200 (:status (submit-form {:body (.getBytes "submitted=true")} cache)))
          (should= "submitted=true" @cache))))

    (context "last-submission"
      (it "displays the content of the last submission"
        (let [cache (atom "submitted=true")]
          (should= 200 (:status (last-submission {} cache)))
          (should= "submitted=true" (:body (last-submission {} cache)))

          (reset! cache "submitted=twice")
          (should= "submitted=twice" (:body (last-submission {} cache)))))))

  (context "options"
    (it "returns a handler to report the allowed methods"
      (let [options-handler (options "GET" "POST" "OPTIONS")]
        (should= {"Allow" "GET,POST,OPTIONS"} (:headers (options-handler {})))))))
