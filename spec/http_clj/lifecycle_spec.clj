(ns http-clj.lifecycle-spec
  (:require [speclj.core :refer :all]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.request :as request]
            [http-clj.response :as response]
            [http-clj.spec-helper.request-generator :refer [GET]]
            [http-clj.logging :as logging]
            [http-clj.connection :as connection]
            [http-clj.lifecycle :refer [write-response
                                        http]])
  (:import java.io.ByteArrayOutputStream))

(def test-log (atom []))

(defrecord TestLogger []
  logging/Logger
  (log [this level contents]
    (swap! test-log #(conj % contents))))

(defn test-app [request]
  (should= "GET" (:method request))
  (response/create request "Message body"))

(describe "the connection lifecycle"
  (with output (ByteArrayOutputStream.))
  (with conn
    (-> (GET "/path" {"User-Agent" "Test Request" "Host" "www.example.com"})
        (mock/socket @output)
        (connection/create)))
  (with application {:entrypoint test-app
                     :logger (->TestLogger)})

  (context "write-response"
    (it "writes the HTTP message to the connection"
      (let [conn (write-response {:body "Message body"
                                  :status 200
                                  :conn @conn})]
        (should-contain "Message body" (.toString @output))
        (should-contain "HTTP/1.1 200 OK\r\n" (.toString @output)))))

  (context "http"
    (it "pushes a request through an application"
      (http @conn @application)
      (should-contain "Message body" (.toString @output)))

    (it "logs the request"
      (http @conn @application)
      (should-contain "GET /path HTTP/1.1" @test-log))

    (it "leaves the connection open"
      (should= false (.isClosed (:socket (http @conn @application)))))))
