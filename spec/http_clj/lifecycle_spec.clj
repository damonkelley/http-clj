(ns http-clj.lifecycle-spec
  (:require [speclj.core :refer :all]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.request :as request]
            [http-clj.response :as response]
            [http-clj.spec-helper.request-generator :refer [GET]]
            [http-clj.lifecycle :refer [request->response
                                        write-response
                                        http]]))

(defn test-app [request]
  (should= "GET" (:method request))
  (response/create request "Message body"))

(describe "the connection lifecycle"
  (with conn
    (-> (GET "/path" {"User-Agent" "Test Request" "Host" "www.example.com"})
        (mock/connection)))

  (context "request->response"
    (it "it returns a response"
      (should= "Message body" (-> @conn
                                  (request->response test-app)
                                  (:body)))))

  (context "write-response"
    (it "writes the HTTP message to the connection"
      (let [conn (write-response {:body "Message body"
                                  :status 200
                                  :conn (mock/connection)})]
        (should-contain "Message body" (:written-to-connection conn))
        (should-contain "HTTP/1.1 200 OK\r\n" (:written-to-connection conn)))))

  (context "http"
    (it "pushes a request through an application"
      (should-contain "Message body" (:written-to-connection (http @conn test-app))))

    (it "it closes the connection"
      (should= false (:open (http @conn test-app))))))
