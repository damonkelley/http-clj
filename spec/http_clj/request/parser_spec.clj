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
      (should= {:method "GET" :path "/" :version "HTTP/1.1" :query-params nil}
               (parser/parse-request-line "GET / HTTP/1.1"))
      (should= {:method "POST" :path "/file" :version "HTTP/1.0" :query-params nil}
               (parser/parse-request-line "POST /file HTTP/1.0")))

    (it "parses the query parameters"
      (let [request-line (parser/parse-request-line "GET /file?key=value HTTP/1.1")]
        (should= {"key" "value"} (:query-params request-line))))

    (it "strips the query string from the path"
      (let [request-line (parser/parse-request-line "GET /file?key=value HTTP/1.1")]
        (should= "/file" (:path request-line))))

    (it "always has :path, :version :method, and :query-params"
      (should= {:method "" :path "" :version "" :query-params nil}
               (parser/parse-request-line ""))))

  (context "parse-path"
    (it "strips the query string from the path"
      (let [request-line {:path "/file?key=value"}]
        (should= "/file" (:path (parser/parse-path request-line)))))

    (it "adds the query parameters to the map"
      (let [request-line {:path "/file?key=value&parameter=other-value"}]
        (should= {"key" "value" "parameter" "other-value"}
                 (:query-params (parser/parse-path request-line)))))

    (it "it decodes parameter fields"
      (let [request-line {:path "/file?operators=%26%2C%3D%3B&sort=true"}]
        (should= {"operators" "&,=;" "sort" "true"}
                 (:query-params (parser/parse-path request-line)))))

    (it ":query-params is nil if there is not query string"
      (let [request-line {:method "GET" :path "/file" :version "HTTP/1.1"}]
        (should= nil
                 (:query-params (parser/parse-path request-line))))))

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
               (parser/parse-field-values {:content-length "10" :host "www.example.com"})))

    (it "parses Range"
      (should= {:range {:units "bytes" :start 0 :end 4}}
               (parser/parse-field-values {:range "bytes=0-4"}))
      (should= {:range {:units "bytes" :start nil :end 60}}
               (parser/parse-field-values {:range "bytes=-60"})))))
