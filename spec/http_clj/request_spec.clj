(ns http-clj.request-spec
  (:require [speclj.core :refer :all]
            [http-clj.request :as request]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.spec-helper.request-generator :refer [POST GET]]
            [http-clj.logging :as logging]
            [clojure.java.io :as io]))

(def test-log (atom []))

(defrecord TestLogger []
  logging/Logger
  (log [this contents]
    (swap! test-log #(conj % contents))))

(describe "request"
  (with get-conn
    (mock/connection
      (GET "/file1"
           {"Host" "www.example.com" "User-Agent" "Test-request"})))

  (with post-conn
    (mock/connection
      (POST "/file2"
            {"Host" "www.example.us" "User-Agent" "Test-request"}
            "var=data")))

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

    (it "has headers"
      (should= "www.example.com"
               (get-in (request/create @get-conn) [:headers "Host"]))

      (should= "www.example.us"
               (get-in (request/create @post-conn) [:headers "Host"]))))

  (context "parse-request-line"
    (it "parses the method"
      (should= "GET" (:method (request/parse-request-line @get-conn)))
      (should= "POST" (:method (request/parse-request-line @post-conn))))

    (it "parses the path"
      (should= "/file1" (:path (request/parse-request-line @get-conn)))
      (should= "/file2" (:path (request/parse-request-line @post-conn))))

    (it "parses the version"
      (should= "HTTP/1.1" (:version (request/parse-request-line @get-conn)))
      (should= "HTTP/1.1" (:version (request/parse-request-line @post-conn)))))

  (context "parse-headers"
    (before (request/parse-request-line @get-conn))

    (it "parses the headers"
      (should= {"Host" "www.example.com" "User-Agent" "Test-request"}
               (request/parse-headers @get-conn)))))
