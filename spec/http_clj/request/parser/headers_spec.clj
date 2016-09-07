(ns http-clj.request.parser.headers-spec
  (:require [speclj.core :refer :all]
            [http-clj.request.parser.headers :as headers]))

(describe "request.parser.headers"
  (context "parse"
    (it "returns an empty map if there are no headers"
      (should= {} (headers/parse [])))

    (it "parses the header lines into key-value pairs"
      (should= {:host "www.example.com" :user-agent "Test-request"}
               (headers/parse ["Host: www.example.com" "User-Agent: Test-request"])))

    (it "parses the header field values"
      (should= {:content-length 10}
               (headers/parse ["Content-Length: 10"]))))

  (context "parse-field-values"
    (it "parses Content-Length"
      (should= {:content-length 9} (headers/parse-field-values {:content-length "9"}))
      (should= {:content-length 10 :host "www.example.com"}
               (headers/parse-field-values {:content-length "10" :host "www.example.com"})))

    (it "parses Range"
      (should= {:range {:units "bytes" :start 0 :end 4}}
               (headers/parse-field-values {:range "bytes=0-4"}))
      (should= {:range {:units "bytes" :start nil :end 60}}
               (headers/parse-field-values {:range "bytes=-60"})))))
