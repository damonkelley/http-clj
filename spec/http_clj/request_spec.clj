(ns http-clj.request-spec
  (:require [speclj.core :refer :all]
            [http-clj.request :as request]
            [http-clj.mock :as mock]
            [clojure.java.io :as io]))

(def get-request (str "GET /file1 HTTP/1.1\r\n"
                      "User-Agent: Test-request\r\n"
                      "Host: www.example.com\r\n"
                      "\r\n"))

(def post-request (str "POST /file2 HTTP/1.0\r\n"
                       "User-Agent: Test-request\r\n"
                       "Host: www.example.us\r\n"
                       "\r\n"
                       "var=data"))

(describe "request"
  (with get-conn (mock/connection get-request))
  (with post-conn (mock/connection post-request))

  (context "when created"
    (it "has the connection"
      (let [conn (mock/connection get-request)]
        (should= conn (:conn (request/create conn)))))

    (it "has the method"
      (should= "GET" (:method (request/create @get-conn)))
      (should= "POST" (:method (request/create @post-conn))))

    (it "has the path"
      (should= "/file1" (:path (request/create @get-conn)))
      (should= "/file2" (:path (request/create @post-conn))))

    (it "has the version"
      (should= "HTTP/1.1" (:version (request/create @get-conn)))
      (should= "HTTP/1.0" (:version (request/create @post-conn))))

    (it "has headers"
      (should= "www.example.com" (get-in
                                   (request/create @get-conn)
                                   [:headers "Host"]))
      (should= "www.example.us" (get-in
                                  (request/create @post-conn)
                                  [:headers "Host"]))))
  (context "parse-request-line"
    (it "parses the method"
      (should= "GET" (:method (request/parse-request-line @get-conn)))
      (should= "POST" (:method (request/parse-request-line @post-conn))))

    (it "parses the path"
      (should= "/file1" (:path (request/parse-request-line @get-conn)))
      (should= "/file2" (:path (request/parse-request-line @post-conn))))

    (it "parses the version"
      (should= "HTTP/1.1" (:version (request/parse-request-line @get-conn)))
      (should= "HTTP/1.0" (:version (request/parse-request-line @post-conn)))))

  (context "parse-headers"
    (before (request/parse-request-line @get-conn))

    (it "parses the headers"
      (should= {"Host" "www.example.com" "User-Agent" "Test-request"}
               (request/parse-headers @get-conn)))))
