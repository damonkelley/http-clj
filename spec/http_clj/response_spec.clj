(ns http-clj.response-spec
  (:require [speclj.core :refer :all]
            [http-clj.response :refer :all]
            [http-clj.spec-helper.mock :as mock]
            [clojure.string :as string])
  (:import java.io.ByteArrayOutputStream))

(describe "a response"
  (with request {:conn (mock/connection)})

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

  (context "write"
    (it "writes the HTTP message to the connection"
      (let [output (ByteArrayOutputStream.)
            conn (write {:body "Message body"
                                  :status 200
                                  :conn (mock/connection "" output)})]
        (should-contain "Message body" (.toString output))
        (should-contain "HTTP/1.1 200 OK\r\n" (.toString output))))))
