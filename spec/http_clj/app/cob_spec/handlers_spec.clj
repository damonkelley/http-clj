(ns http-clj.app.cob-spec.handlers-spec
  (:require [speclj.core :refer :all]
            [http-clj.app.cob-spec.handlers :refer :all]
            [http-clj.request-handler.filesystem :as filesystem]
            [http-clj.file :as file]
            [clojure.java.io :as io]
            [clojure.data.codec.base64 :as b64])
  (:import java.io.ByteArrayOutputStream))

(describe "handlers"
  (context "static"
    (with dir "resources/static/")
    (it "responds with a listing if directory"
      (let [resp (static {:method "GET" :path "/"} @dir)]
        (should-contain "image.gif" (:body resp))))

    (it "responds with a file if it is a file"
      (let [resp (static {:method "GET" :path "/file.txt"} @dir)]
        (should-contain "File contents" (String. (:body resp)))))

    (it "responds with a 409 when a PATCH precondition fails"
      (let [request {:method "PATCH"
                     :path "/file.txt"
                     :headers {:if-match "incorrect-etag"}}]
        (should= 409 (:status (static request @dir)))))

    (it "will patch a file"
      (let [contents (file/binary-slurp "resources/static/file.txt")
            request {:method "PATCH" :path "/file.txt" :body contents}]
        (should= 204 (:status (static request @dir)))))

    (it "responds with 404 if a file is not found"
      (let [request{:method "GET" :path "/does-not-exist.txt"}]
        (should= 404 (:status (static request @dir)))))

    (it "responds to HEAD requests when the path is a directory"
      (let [request {:method "HEAD" :path "/"}]
        (should= 200 (:status (static request @dir)))))

    (it "responds to HEAD requests when the path is a file"
      (let [request {:method "HEAD" :path "/file.txt"}]
        (should= 200 (:status (static request @dir))))))

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
        (should= {"Allow" "GET,POST,OPTIONS"} (:headers (options-handler {}))))))

  (context "parameters"
    (it "responds with the parameters formatted in the body"
      (let [request {:query-params {"parameter-1" "a"
                                    "parameter-2" "b"}}]
        (should-contain "parameter-1 = a\n" (:body (parameters request)))
        (should-contain "parameter-2 = b" (:body (parameters request))))))

  (context "redirect-to-root"
    (it "redirects to the root"
      (let [request {:headers {:host "host:port"}}
            resp (redirect-to-root request)]
        (should= 302 (:status resp))
        (should= "http://host:port/" (get-in resp [:headers :location]))))))
