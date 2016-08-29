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
                   {"Host" "www.example.us" "User-Agent" "Test-request"}
                   "var=data"))})

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

     (context "parse-headers"
       (before (parser/request-line @get-request))

       (it "parses the headers"
         (should= {"Host" "www.example.com" "User-Agent" "Test-request"}
                  (parser/headers @get-request)))))
