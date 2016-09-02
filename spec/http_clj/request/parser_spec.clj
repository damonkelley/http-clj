(ns http-clj.request.parser-spec
  (:require [speclj.core :refer :all]
            [http-clj.request :as request]
            [http-clj.request.parser :as parser]
            [http-clj.request.reader :as reader]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.spec-helper.request-generator :refer [GET POST]]))

(describe "request.parser"
  (tags "parser")
  (context "parse-request-line"
    (it "parses the request line into a map"
      (should= {:method "GET" :path "/" :version "HTTP/1.1"}
               (parser/parse-request-line "GET / HTTP/1.1"))
      (should= {:method "POST" :path "/file" :version "HTTP/1.0"}
               (parser/parse-request-line "POST /file HTTP/1.0")))

    (it "always has path version and method keys"
      (should= {:method "" :path "" :version ""} (parser/parse-request-line ""))))

  (context "parse-headers"
    (it "returns an empty map if there are no headers"
      (should= {} (parser/parse-headers [])))

    (it "parses the header lines into key-value pairs"
      (should= {:host "www.example.com" :user-agent "Test-request"}
               (parser/parse-headers ["Host: www.example.com" "User-Agent: Test-request"])))

    (it "parses the header field values"
      (should= {:content-length 10}
               (parser/parse-headers ["Content-Length: 10"]))))

  (context "parse-field-values"
    (it "parses Content-Length"
      (should= {:content-length 9} (parser/parse-field-values {:content-length "9"}))
      (should= {:content-length 10 :host "www.example.com"}
               (parser/parse-field-values {:content-length "10" :host "www.example.com"})))))
