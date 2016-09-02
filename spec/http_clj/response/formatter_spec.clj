(ns http-clj.response.formatter-spec
  (:require [speclj.core :refer :all]
            [http-clj.response.formatter :refer :all]
            [http-clj.response :as response]
            [http-clj.spec-helper.mock :as mock]
            [clojure.string :as string]))

(defn byte-array->string [array]
  (String. array))

(defn get-status-line [message]
  (first (string/split (byte-array->string message) #"\r\n")))

(describe "response.formatter"
  (context "format-message"
    (with request {:conn (mock/connection)})

    (it "is a valid HTTP response"
      (let [headers {"Accept" "*" "Host" "www.example.com"}
            response (response/create @request "Hello, world!" :headers headers)
            {message :message} (format-message response)]
        (should= (str "HTTP/1.1 200 OK\r\n"
                      "Accept: *\r\n"
                      "Host: www.example.com\r\n"
                      "\r\n"
                      "Hello, world!")
                 (byte-array->string message))))

    (it "formats the headers"
      (let [headers {"Host" "www.example.com" "Content-Type" "application/json"}
            response (response/create @request "Message body" :headers headers)
            {message :message} (format-message response)]
        (should-contain "Host: www.example.com\r\n" (byte-array->string message))
        (should-contain "Content-Type: application/json" (byte-array->string message))))

    (it "uses the body from the response"
      (let [response (response/create @request "Message body")
            {message :message} (format-message response)]
        (should-contain "Message body" (byte-array->string message))))

    (it "uses the body from the response"
      (let [response (response/create @request (.getBytes "Message body"))
            {message :message} (format-message response)]
        (should-contain "Message body" (byte-array->string message))))

    (it "uses the status from the response"
      (let [response (response/create @request "" :status 404)
            {message :message} (format-message response)]
        (should-contain "404" (byte-array->string message))))

    (it "has a status line for a GET request"
      (let [response (response/create @request "" :status 200)
            {message :message} (format-message response)]
        (should= "HTTP/1.1 200 OK" (get-status-line message))))

    (it "has a status line for a GET request"
      (let [response (response/create @request "" :status 404)
            {message :message} (format-message response)]
        (should= "HTTP/1.1 404 Not Found" (get-status-line message))))))
