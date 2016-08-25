(ns http-clj.request.parser-spec
  (:require [speclj.core :refer :all]
            [http-clj.request :as request]
            [http-clj.request.parser :as parser]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.spec-helper.request-generator :refer [GET POST]]))

(describe "request.parser"
  (with get-conn
    (mock/connection
      (GET "/file1"
           {"Host" "www.example.com" "User-Agent" "Test-request"})))

  (with post-conn
    (mock/connection
      (POST "/file2"
            {"Host" "www.example.us" "User-Agent" "Test-request"}
            "var=data")))

  (context "parse-request-line"
    (it "parses the method"
      (should= "GET" (:method (parser/request-line @get-conn)))
      (should= "POST" (:method (parser/request-line @post-conn))))

    (it "parses the path"
      (should= "/file1" (:path (parser/request-line @get-conn)))
      (should= "/file2" (:path (parser/request-line @post-conn))))

    (it "parses the version"
      (should= "HTTP/1.1" (:version (parser/request-line @get-conn)))
      (should= "HTTP/1.1" (:version (parser/request-line @post-conn)))))

  (context "parse-headers"
    (before (parser/request-line @get-conn))

    (it "parses the headers"
      (should= {"Host" "www.example.com" "User-Agent" "Test-request"}
               (parser/headers @get-conn)))))
