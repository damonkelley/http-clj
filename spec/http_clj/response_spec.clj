(ns http-clj.response-spec
  (:require [speclj.core :refer :all]
            [http-clj.response :refer [create compose]]
            [http-clj.mock :refer [connection]]))

(def request {:conn (connection)})

(describe "a response"
  (context "when created"
    (it "has a body"
      (should= "Hello, world!" (:body (create request "Hello, world!"))))

    (it "has the connection"
      (should= (:conn request) (:conn (create request "")))))

  (context "when composed"
    (it "is a valid HTTP response"
      (let [expected (str "HTTP/1.1 200 OK\r\n\r\n"
                          "Hello, world!\r\n")]

        (should= expected (-> (create request "Hello, world!")
                              (compose)
                              (:message)))))

    (it "uses the body from the response"
      (should-contain "Message body" (-> (create request "Message body")
                                         (compose)
                                         (:message))))))
