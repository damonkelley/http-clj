(ns http-clj.response-spec
  (:require [speclj.core :refer :all]
            [http-clj.response :refer [create compose]]
            [http-clj.mock :refer [connection]]))

(describe "a response"
  (context "when created"
    (it "has a body"
      (should= "Hello, world!" (:body (create "Hello, world!")))))

  (context "when composed"
    (it "is a valid HTTP response"
      (let [expected (str "HTTP/1.1 200 OK\r\n\r\n"
                          "Hello, world!\r\n")]
        (should= expected (-> (create "Hello, world!")
                              (compose)))))

    (it "uses the body from the response"
      (should-contain "Message body" (-> (create "Message body")
                                         (compose))))))

