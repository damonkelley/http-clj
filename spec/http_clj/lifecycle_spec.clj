(ns http-clj.lifecycle-spec
  (:require [speclj.core :refer :all]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.request :as request]
            [http-clj.response :as response]
            [http-clj.spec-helper.request-generator :refer [GET]]
            [http-clj.logging :as logging]
            [http-clj.lifecycle :refer [write-response
                                        http]]))

(def test-log (atom []))

(defrecord TestLogger []
  logging/Logger
  (log [this contents]
    (swap! test-log #(conj % contents))))

(defn test-app [request]
  (should= "GET" (:method request))
  (response/create request "Message body"))

(describe "the connection lifecycle"
  (with conn
    (-> (GET "/path" {"User-Agent" "Test Request" "Host" "www.example.com"})
        (mock/connection)))
  (with application {:entrypoint test-app
                     :logger (->TestLogger)})

  (context "write-response"
    (it "writes the HTTP message to the connection"
      (let [conn (write-response {:body "Message body"
                                  :status 200
                                  :conn (mock/connection)})]
        (should-contain "Message body" (:written-to-connection conn))
        (should-contain "HTTP/1.1 200 OK\r\n" (:written-to-connection conn)))))

  (context "http"
    (it "pushes a request through an application"
      (should-contain "Message body" (:written-to-connection (http @conn @application))))

    (it "logs the request"
      (http @conn @application)
      (should-contain "GET /path HTTP/1.1" @test-log))

    (it "leaves the connection open"
      (should= true (:open (http @conn @application))))))
