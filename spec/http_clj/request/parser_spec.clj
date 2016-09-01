(ns http-clj.request.parser-spec
  (:require [speclj.core :refer :all]
            [http-clj.request :as request]
            [http-clj.request.parser :as parser]
            [http-clj.request.reader :as reader]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.spec-helper.request-generator :refer [GET POST]]))

(describe "request.parser"
  (with get-request
    {:conn  (mock/connection
              (GET "/file1"
                   {"Host" "www.example.com" "User-Agent" "Test-request"}))})

  (with post-request
    {:conn (mock/connection
             (POST "/file2"
                   {"Host" "www.example.us"
                    "User-Agent" "Test-request"
                    "Content-Length" 8}
                   "var=data"))})

  (context "parse-request-line"
    (it "parses the method"
      (should= "GET" (:method (parser/parse-request-line "GET / HTTP/1.1")))
      (should= "POST" (:method (parser/parse-request-line "POST / HTTP/1.1"))))

    (it "parses the path"
      (should= "/file1" (:path (parser/parse-request-line "GET /file1 HTTP/1.1")))
      (should= "/file2" (:path (parser/parse-request-line "GET /file2 HTTP/1.1"))))

    (it "parses the version"
      (should= "HTTP/1.1" (:version (parser/parse-request-line "GET / HTTP/1.1")))
      (should= "HTTP/1.0" (:version (parser/parse-request-line "GET / HTTP/1.0")))))

  (context "parse-headers"
    (it "returns an empty map if there are no headers"
      (should= {} (parser/parse-headers [])))

    (it "parses the header lines into key-value pairs"
      (should= {"Host" "www.example.com" "User-Agent" "Test-request"}
               (parser/parse-headers ["Host: www.example.com" "User-Agent: Test-request"])))

    (it "parses the header field values"
      (should= {"Content-Length" 10}
               (parser/parse-headers ["Content-Length: 10"]))))

  (context "parse-header-fields"
    (it "parses Content-Length"
      (should= {"Content-Length" 9} (parser/parse-header-fields {"Content-Length" "9"}))
      (should= {"Content-Length" 10 "Host" "www.example.com"}
               (parser/parse-header-fields {"Content-Length" "10" "Host" "www.example.com"})))))
