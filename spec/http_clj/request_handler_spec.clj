(ns http-clj.request-handler-spec
  (:require [speclj.core :refer :all]
            [http-clj.request-handler :as handler]
            [http-clj.response :as response]
            [clojure.java.io :as io]
            [hiccup.core :refer [html]])
  (:import java.io.File))


(defn mock-directory []
  (proxy [File] [""]
    (listFiles []
      (into-array [(io/file "file-a")
                   (io/file "file-b")
                   (io/file "subdirectory")]))
    (isDirectory []
      true)))

(describe "handler"
  (context "directory"
    (it "has a text/html content type"
      (let [{headers :headers} (handler/directory {:path "/"} (mock-directory))]
      (should= "text/html" (get headers "Content-Type"))))

    (it "lists the contents of the directory"
      (let [{body :body} (handler/directory {:path "/"} (mock-directory))]
        (should-contain (html [:a {:href "/file-a"} "file-a"]) body)
        (should-contain (html [:a {:href "/file-b"} "file-b"]) body)
        (should-contain (html [:a {:href "/subdirectory"} "subdirectory"]) body))))

    (it "paths to the files are relative to the request path"
      (let [{body :body} (handler/directory {:path "/dir"} (mock-directory))]
        (should-contain (html [:a {:href "/dir/file-a"} "file-a"]) body)))

  (context "not-found"
    (it "responds with status code 404"
      (should= 404 (:status (handler/not-found {}))))

    (it "has Not Found in the body"
      (should= "Not Found" (:body (handler/not-found {})))))

  (context "method-not-allowed"
    (it "responds with status code 405"
      (should= 405 (:status (handler/method-not-allowed {}))))

    (it "has Method Not Allowed in the body"
      (should= "Method Not Allowed" (:body (handler/method-not-allowed {})))))

  (context "file"
    (with test-path "/tmp/http-clj-test-file-handler")
    (with test-data "Some content")
    (before-all (spit @test-path @test-data))

    (it "returns a handler when given just a path"
      (let [{message :body} ((handler/file @test-path) {})]
        (should= (seq message) (seq (.getBytes @test-data)))))

    (it "can accept a request and a file object"
      (let [{message :body} (handler/file {} (io/file @test-path))]
        (should= (seq message) (seq (.getBytes @test-data))))))

  (context "head"
    (it "returns a resp with the body stripped out"
      (let [handler #(response/create % "Body" :status 200)
            resp (handler/head handler {})]
      (should= nil (:body resp))))

    (it "keeps the headers"
      (let [handler #(response/create % "Body" :headers {"Host" "www.example.com"})
            resp (handler/head handler {})]
      (should= {"Host" "www.example.com"} (:headers resp))
      (should= 200 (:status resp)))))

  (it "keeps the status"
    (let [handler #(response/create % "Body" :status 201 :headers {"User-Agent" "test-agent"})
          resp (handler/head handler {})]
      (should= {"User-Agent" "test-agent"} (:headers resp)))))
