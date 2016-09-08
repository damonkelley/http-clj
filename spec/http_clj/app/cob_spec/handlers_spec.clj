(ns http-clj.app.cob-spec.handlers-spec
  (:require [speclj.core :refer :all]
            [http-clj.app.cob-spec.handlers :refer :all]
            [http-clj.request-handler.filesystem :as filesystem]
            [http-clj.file :as file]
            [clojure.java.io :as io]
            [clojure.data.codec.base64 :as b64])
  (:import java.io.ByteArrayOutputStream))

(describe "handlers"
  (describe "static"
    (with dir "resources/static/")
    (it "responds with a listing if directory"
      (let [resp (static {:method "GET" :path "/"} @dir)]
        (should-contain "image.gif" (:body resp))))

    (it "responds with a file if it is a file"
      (let [resp (static {:method "GET" :path "/file.txt"} @dir)]
        (should-contain "File contents" (String. (:body resp)))))

    (it "responds with a 409 when a PATCH precondition fails"
      (let [request {:method "PATCH" :path "/file.txt" :headers {:if-match "incorrect-etag"}}
            resp (static request @dir)]
        (should= 409 (:status resp))))

    (it "will patch a file"
      (let [contents (file/binary-slurp "resources/static/file.txt")
            request {:method "PATCH" :path "/file.txt" :body contents}
            resp (static request @dir)]
        (should= 204 (:status resp))))

    (it "responds with 404 if a file is not found"
      (let [resp (static {:method "GET" :path "/file-that-does-not-exist.txt"} @dir)]
        (should= 404 (:status resp)))))

  (describe "log"
    (with output (ByteArrayOutputStream.))
    (with request-log (io/writer @output))

    (it "displays the log"
      (.write @request-log "message 1\n")
      (.flush @request-log)
      (should= 200 (:status (log {} @output)))
      (should-contain "message 1\n" (:body (log {} @output)))))

  (context "form"
    (describe "submit-form"
      (with cache (atom ""))

      (it "updates the form"
        (should= 200 (:status (submit-form {:body (.getBytes "submitted=true")} @cache)))
        (should= "submitted=true" @@cache)))

    (describe "last-submission"
      (with cache (atom "submitted=true"))

      (it "displays the content of the last submission"
        (should= 200 (:status (last-submission {} @cache)))
        (should= "submitted=true" (:body (last-submission {} @cache)))

        (reset! @cache "submitted=twice")
        (should= "submitted=twice" (:body (last-submission {} @cache)))))

    (describe "clear-submission"
      (with cache (atom "count=20"))

      (it "responds with a 200"
        (should= 200 (:status (clear-submission {} @cache))))

      (it "clears the form cache"
        (clear-submission {} @cache)
        (should= "" @@cache))))

  (describe "no-coffee"
    (it "responds with 418"
      (should= 418 (:status (no-coffee {}))))

    (it "has content in the body"
      (should= "I'm a teapot" (:body (no-coffee {})))))

  (describe "tea"
    (it "responds with 200"
      (should= 200 (:status (tea {})))))

  (describe "options"
    (it "returns a handler to report the allowed methods"
      (let [options-handler (options "GET" "POST" "OPTIONS")]
        (should= {"Allow" "GET,POST,OPTIONS"} (:headers (options-handler {}))))))

  (describe "parameters"
    (it "responds with the parameters formatted in the body"
      (let [request {:query-params {"parameter-1" "a"
                                    "parameter-2" "b"}}]
        (should-contain "parameter-1 = a\n" (:body (parameters request)))
        (should-contain "parameter-2 = b" (:body (parameters request))))))

  (describe "redirect-to-root"
    (it "redirects to the root"
      (let [request {:headers {:host "host:port"}}
            resp (redirect-to-root request)]
        (should= 302 (:status resp))
        (should= "http://host:port/" (get-in resp [:headers :location]))))))
