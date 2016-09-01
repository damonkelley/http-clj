(ns http-clj.request-spec
  (:require [speclj.core :refer :all]
            [http-clj.request :as request]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.spec-helper.request-generator :refer [POST GET]]))

(describe "request"
  (with get-conn
    (mock/connection
      (GET "/file1"
           {"Host" "www.example.com" "User-Agent" "Test-request"})))

  (with post-conn
    (mock/connection
      (POST "/file2"
            {"Host" "www.example.us"
             "User-Agent" "Test-request"
             "Content-Length" 8}
            "var=data")))

  (context "get-request-line"
    (it "parses the method"
      (should= "GET" (:method (request/get-request-line {:conn @get-conn})))
      (should= "POST" (:method (request/get-request-line {:conn @post-conn}))))

    (it "parses the path"
      (should= "/file1" (:path (request/get-request-line {:conn @get-conn})))
      (should= "/file2" (:path (request/get-request-line {:conn @post-conn}))))

    (it "parses the version"
      (should= "HTTP/1.1" (:version (request/get-request-line {:conn @get-conn})))
      (should= "HTTP/1.1" (:version (request/get-request-line {:conn @post-conn})))))

  (context "when created"
    (it "has the connection"
      (should= @get-conn (:conn (request/create @get-conn))))

    (it "has the method"
      (should= "GET" (:method (request/create @get-conn)))
      (should= "POST" (:method (request/create @post-conn))))

    (it "has the path"
      (should= "/file1" (:path (request/create @get-conn)))
      (should= "/file2" (:path (request/create @post-conn))))

    (it "has the version"
      (should= "HTTP/1.1" (:version (request/create @get-conn)))
      (should= "HTTP/1.1" (:version (request/create @post-conn))))

    (it "has the headers"
      (let [request (request/create @get-conn)]
        (should= "www.example.com" (get-in request [:headers "Host"])))

      (let [request (request/create @post-conn)]
        (should= 8 (get-in request [:headers "Content-Length"]))))

    (it "has the body"
      (should= nil (:body (request/create @get-conn)))
      (should= "var=data" (String. (:body (request/create @post-conn)))))))
