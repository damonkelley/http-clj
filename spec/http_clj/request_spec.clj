(ns http-clj.request-spec
  (:require [speclj.core :refer :all]
            [http-clj.request :as request]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.spec-helper.request-generator :refer [POST GET]]))

(defn test-get-conn []
  (->> ["/file1" {"Host" "www.example.com" "User-Agent" "Test-request"}]
        (apply GET)
        mock/connection))

(defn test-post-conn []
  (->> ["/file2" {"Host" "www.example.us" "Content-Length" 8} "var=data"]
       (apply POST)
       mock/connection))

(describe "request"
  (with get-conn (test-get-conn))
  (with post-conn (test-post-conn))

  (context "get-request-line"
    (it "parses the request-line"
      (should= {:method "GET" :path "/file1" :version "HTTP/1.1"}
               (request/get-request-line {:conn @get-conn}))

      (should= {:method "POST" :path "/file2" :version "HTTP/1.1"}
               (request/get-request-line {:conn @post-conn}))))

  (context "get-headers"
    (before (request/get-request-line  {:conn @get-conn}))
    (before (request/get-request-line {:conn @post-conn}))

    (it "reads the headers from the connection into a map"
      (should= {"Host" "www.example.com" "User-Agent" "Test-request"}
               (request/get-headers {:conn @get-conn}))

      (should= {"Host" "www.example.us" "Content-Length" 8}
               (request/get-headers {:conn @post-conn}))))

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
      (should= "var=data" (String. (:body (request/create @post-conn)))))

    (it "validates the request"
      (should= true (:valid? (request/create @get-conn)))
      (should= false (:valid? (request/create (mock/connection "")))))))
