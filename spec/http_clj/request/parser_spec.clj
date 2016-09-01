(ns http-clj.request.parser-spec
  (:require [speclj.core :refer :all]
            [http-clj.request :as request]
            [http-clj.request.parser :as parser]
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

  (context "readline"
    (it "reads line delimited by newline characters"
      (let [conn (mock/connection "one\ntwo")]
        (should= "one" (parser/readline conn))
        (should= "two" (parser/readline conn))))

    (it "reads line delimited by carriage returns"
      (let [conn (mock/connection "one\r\ntwo")]
        (should= "one" (parser/readline conn))
        (should= "two" (parser/readline conn)))))

  (context "parse-request-line"
    (it "parses the method"
      (should= "GET" (:method (parser/request-line @get-request)))
      (should= "POST" (:method (parser/request-line @post-request))))

    (it "parses the path"
      (should= "/file1" (:path (parser/request-line @get-request)))
      (should= "/file2" (:path (parser/request-line @post-request))))

    (it "parses the version"
      (should= "HTTP/1.1" (:version (parser/request-line @get-request)))
      (should= "HTTP/1.1" (:version (parser/request-line @post-request)))))

  (context "headers"
    (before (parser/request-line @get-request))
    (before (parser/request-line @post-request))

    (it "reads the headers from the connection into a map"
      (should= {"Host" "www.example.com" "User-Agent" "Test-request"}
               (parser/headers @get-request)))

    (it "parses the header field values"
         (let [headers (parser/headers @post-request)]
           (should= 8 (get headers "Content-Length"))))

    (context "read-headers"
      (it "reads the headers into a list"
        (should= ["Host: www.example.com" "User-Agent: Test-request"]
                 (parser/read-headers @get-request))))

    (context "parse-headers"
      (it "returns an empty map if there are no headers"
        (should= {} (parser/parse-headers [])))
      (it "parses the header lines into key-value pairs"
        (should= {"Host" "www.example.com" "Content-Length" "8"}
                 (parser/parse-headers ["Host: www.example.com" "Content-Length: 8"]))))

    (context "parse-header-fields"
        (it "parses Content-Length"
          (should= {"Content-Length" 9} (parser/parse-header-fields {"Content-Length" "9"}))
          (should= {"Content-Length" 10 "Host" "www.example.com"}
                   (parser/parse-header-fields {"Content-Length" "10" "Host" "www.example.com"})))))

  (context "read-body"
    (it "is nil if the there is not body to read"
      (let [request (-> @get-request
                        (merge  (parser/request-line @get-request))
                        (assoc :headers (parser/headers @get-request)))]
        (should= nil (parser/read-body request))))
    (it "returns the body if present"
      (let [request (-> @post-request
                        (merge  (parser/request-line @post-request))
                        (assoc :headers (parser/headers@post-request)))]
        (should= "var=data" (String. (parser/read-body request)))))))
