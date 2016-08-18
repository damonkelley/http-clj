(ns http-clj.handler-spec
  (:require [speclj.core :refer :all]
            [http-clj.handler :as handler]))

(describe "file-handler"
  (with test-path "/tmp/http-clj-test-file-handler")
  (with test-data "Some content")

  (it "returns a handler"
    (spit @test-path @test-data)
    (let [{message :body} ((handler/file @test-path) {})]
      (should= (seq message) (seq (.getBytes @test-data))))))
