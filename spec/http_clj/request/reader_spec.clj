(ns http-clj.request.reader-spec
  (:require [speclj.core :refer :all]
            [http-clj.request :as request]
            [http-clj.request.reader :as reader]
            [http-clj.request.parser :as parser]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.spec-helper.request-generator :refer [GET POST]]))

(describe "request.reader"
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
        (should= "one" (reader/readline {:conn conn}))
        (should= "two" (reader/readline {:conn conn}))))

    (it "reads line delimited by carriage returns"
      (let [conn (mock/connection "one\r\ntwo")]
        (should= "one" (reader/readline {:conn conn}))
        (should= "two" (reader/readline {:conn conn})))))

  (context "read-headers"
    (before (reader/readline @get-request))
    (before (reader/readline @post-request))

      (it "reads the headers into a list"
        (should= ["Host: www.example.com" "User-Agent: Test-request"]
                 (reader/read-headers @get-request))))

  (context "read-body"
    (it "is nil if the there is not body to read"
      (let [request (-> @get-request
                        (merge  (request/get-request-line @get-request))
                        (assoc :headers (parser/headers @get-request)))]
        (should= nil (reader/read-body request))))

    (it "returns the body if present"
      (let [request (-> @post-request
                        (merge  (request/get-request-line @post-request))
                        (assoc :headers (parser/headers@post-request)))]
        (should= "var=data" (String. (reader/read-body request)))))))
