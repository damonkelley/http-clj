(ns http-clj.request-handler-spec
  (:require [speclj.core :refer :all]
            [http-clj.request-handler :as handler]))

(describe "handler"
  (context "not-found"
    (it "responds with status code 404"
      (should= 404 (:status (handler/not-found {}))))
    (it "has Not Found in the body"
      (should= "Not Found" (:body (handler/not-found {})))))

  (context "file"
    (with test-path "/tmp/http-clj-test-file-handler")
    (with test-data "Some content")

    (it "returns a handler"
      (spit @test-path @test-data)
      (let [{message :body} ((handler/file @test-path) {})]
        (should= (seq message) (seq (.getBytes @test-data)))))))
