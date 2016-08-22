(ns http-clj.request-handler-spec
  (:require [speclj.core :refer :all]
            [http-clj.request-handler :as handler]
            [clojure.java.io :as io]
            [hiccup.core :refer [html]])
  (:import java.io.File))


(defn mock-directory
  ([] (mock-directory "some-path"))
  ([path] (proxy [File] [path]
            (list []
              (into-array ["file-a" "file-b" "subdirectory"]))
            (isDirectory []
              true))))

(describe "handler"
  (context "directory"
    (it "has a text/html content type"
      (should= "text/html"
               (get-in (handler/directory {} (mock-directory)) [:headers "Content-Type"])))

    (it "lists the contents of the directory"
      (let [contents ["file-a" "file-b" "subdirectory"]
            {body :body} (handler/directory {} (mock-directory))]
        (should-contain (html [:a {:href "file-a"} "file-a"]) body)
        (should-contain (html [:a {:href "file-b"} "file-b"]) body)
        (should-contain (html [:a {:href "subdirectory"} "subdirectory"]) body))))

  (context "not-found"
    (it "responds with status code 404"
      (should= 404 (:status (handler/not-found {}))))
    (it "has Not Found in the body"
      (should= "Not Found" (:body (handler/not-found {})))))

  (context "file"
    (with test-path "/tmp/http-clj-test-file-handler")
    (with test-data "Some content")
    (before-all (spit @test-path @test-data))

    (it "returns a handler when given just a path"
      (let [{message :body} ((handler/file @test-path) {})]
        (should= (seq message) (seq (.getBytes @test-data)))))

    (it "can accept a request and a file object"
      (let [{message :body} (handler/file {} (io/file @test-path))]
        (should= (seq message) (seq (.getBytes @test-data)))))))
