(ns http-clj.protocol-spec
  (:require [speclj.core :refer :all]
            [http-clj.spec-helper.mock :as mock]
            [http-clj.response :as response]
            [http-clj.spec-helper.request-generator :refer [GET]]
            [http-clj.logging :as logging]
            [http-clj.connection :as connection]
            [http-clj.protocol :refer [http]])
  (:import java.io.ByteArrayOutputStream))

(def test-log (atom []))

(defrecord TestLogger []
  logging/Logger
  (log [this level contents]
    (swap! test-log #(conj % contents))))

(defn test-app [request]
  (should= "GET" (:method request))
  (response/create request "Message body"))

(describe "protocol"
  (with output (ByteArrayOutputStream.))
  (with conn
    (-> (GET "/path" {"User-Agent" "Test Request" "Host" "www.example.com"})
        (mock/socket @output)
        (connection/create)))
  (with application {:entrypoint test-app
                     :logger (->TestLogger)})

  (context "http"
    (it "pushes a request through an application"
      (http @conn @application)
      (should-contain "Message body" (.toString @output)))

    (it "validates requests"
      (let [invalid-request (-> (GET "" {"User-Agent" "Test Request" "Host" "www.example.com"})
                                (mock/socket @output)
                                (connection/create))]
        (http invalid-request @application)
        (should-contain "HTTP/1.1 400" (.toString @output))))

    (it "logs the request"
      (http @conn @application)
      (should-contain "GET /path HTTP/1.1" @test-log))

    (it "leaves the connection open"
      (should= false (.isClosed (:socket (http @conn @application)))))))
