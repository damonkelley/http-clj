(ns http-clj.request.reader-spec
  (:require [speclj.core :refer :all]
            [http-clj.request.reader :as reader]
            [http-clj.request.parser :as parser]
            [http-clj.spec-helper.mock :as mock]))

(describe "request.reader"
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
    (it "returns an empty list if there are no headers"
      (let [conn (mock/connection (str "\r\n"
                                       "Body"))]
        (should= [] (reader/read-headers {:conn conn}))))

    (it "reads the headers into a list"
      (let [conn (mock/connection (str "Host: www.example.com\r\n"
                                       "User-Agent: Test-request\r\n"
                                       "\r\n"
                                       "Body"))]
      (should= ["Host: www.example.com" "User-Agent: Test-request"]
               (reader/read-headers {:conn conn})))))

  (context "read-body"
    (it "is nil if the there is not body to read"
      (let [request {:conn (mock/connection "")
                     :headers {}}]
        (should= nil (reader/read-body request))))

    (it "returns the body if present"
      (let [request {:conn (mock/connection "body to read")
                     :headers {:content-length (alength (.getBytes "body to read"))}}]
        (should= "body to read" (String. (reader/read-body request)))))))
