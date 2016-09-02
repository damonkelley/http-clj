(ns http-clj.response-spec
  (:require [speclj.core :refer :all]
            [http-clj.response :refer :all]
            [http-clj.spec-helper.mock :refer [connection]]
            [http-clj.spec-helper.request-generator :refer [GET]]
            [clojure.string :as string])
  (:import java.io.ByteArrayOutputStream))

(defn byte-array->string [array]
  (String. array))

(defn get-status-line [message]
  (first (string/split (byte-array->string message) #"\r\n")))

(describe "a response"
  (with request {:conn (connection)})

  (context "when created"
    (it "has a body"
      (should= "Hello, world!" (:body (create @request "Hello, world!"))))

    (it "has a status code"
      (should= 200 (:status (create @request "Body")))
      (should= 404 (:status (create @request "Body" :status 404))))

    (it "has default headers"
      (should= {} (:headers (create @request ""))))

    (it "can be provided with headers"
      (let [{headers :headers} (create @request "" :headers {"Content-Type" "text/html"})]
        (should= {"Content-Type" "text/html"} headers)))

    (it "has the connection"
      (should= (:conn @request) (:conn (create @request "")))))

  (context "when composed"
    (it "is a valid HTTP response"
      (let [headers {"Accept" "*" "Host" "www.example.com"}
            response (create @request "Hello, world!" :headers headers)
            {message :message} (compose response)]
        (should= (str "HTTP/1.1 200 OK\r\n"
                      "Accept: *\r\n"
                      "Host: www.example.com\r\n"
                      "\r\n"
                      "Hello, world!")
                 (byte-array->string message))))

    (it "formats the headers"
      (let [headers {"Host" "www.example.com" "Content-Type" "application/json"}
            response (create @request "Message body" :headers headers)
            {message :message} (compose response)]
        (should-contain "Host: www.example.com\r\n" (byte-array->string message))
        (should-contain "Content-Type: application/json" (byte-array->string message))))

    (it "uses the body from the response"
      (let [response (create @request "Message body")
            {message :message} (compose response)]
        (should-contain "Message body" (byte-array->string message))))

    (it "uses the body from the response"
      (let [response (create @request (.getBytes "Message body"))
            {message :message} (compose response)]
        (should-contain "Message body" (byte-array->string message))))

    (it "uses the status from the response"
      (let [response (create @request "" :status 404)
            {message :message} (compose response)]
        (should-contain "404" (byte-array->string message))))

    (it "has a status line for a GET request"
      (let [response (create @request "" :status 200)
            {message :message} (compose response)]
        (should= "HTTP/1.1 200 OK" (get-status-line message))))

    (it "has a status line for a GET request"
      (let [response (create @request "" :status 404)
            {message :message} (compose response)]
        (should= "HTTP/1.1 404 Not Found" (get-status-line message)))))

  (context "write"
    (it "writes the HTTP message to the connection"
      (let [output (ByteArrayOutputStream.)
            conn (write {:body "Message body"
                                  :status 200
                                  :conn (connection "" output)})]
        (should-contain "Message body" (.toString output))
        (should-contain "HTTP/1.1 200 OK\r\n" (.toString output))))))
