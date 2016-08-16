(ns http-clj.response-spec
  (:require [speclj.core :refer :all]
            [http-clj.response :refer [create compose]]
            [http-clj.spec-helper.mock :refer [connection]]
            [clojure.string :as string]))

(def request {:conn (connection)})

(defn get-status-line [message]
  (first (string/split message #"\r\n")))

(describe "a response"
  (with request {:conn (connection)})

  (context "when created"
    (it "has a body"
      (should= "Hello, world!" (:body (create request "Hello, world!"))))

    (it "has a status code"
      (should= 200 (:status (create request "Body")))
      (should= 404 (:status (create request "Body" :status 404))))

    (it "has the connection"
      (should= (:conn request) (:conn (create request "")))))

  (context "when composed"
    (it "is a valid HTTP response"
      (let [expected (str "HTTP/1.1 200 OK\r\n"
                          "\r\n"
                          "Hello, world!")]
        (should= expected (-> (create request "Hello, world!")
                              (compose)
                              (:message)))))

    (it "uses the body from the response"
      (should-contain "Message body" (-> (create request "Message body")
                                         (compose)
                                         (:message))))
    (it "uses the status from the response"
      (should-contain "404" (-> (create request "" :status 404)
                                (compose)
                                (:message))))

    (it "has a status line for a GET request"
      (let [{message :message} (compose (create @request "" :status 200))]
        (should= "HTTP/1.1 200 OK" (get-status-line message))))

    (it "has a status line for a GET request"
      (let [{message :message} (compose (create @request "" :status 404))]
        (should= "HTTP/1.1 404 Not Found" (get-status-line message))))))
