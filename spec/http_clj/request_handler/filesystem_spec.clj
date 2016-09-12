(ns http-clj.request-handler.filesystem-spec
  (:require [speclj.core :refer :all]
            [http-clj.request-handler.filesystem :as handler]
            [http-clj.response :as response]
            [http-clj.entity :as entity]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io])
  (:import java.io.File))


(defn mock-directory []
  (proxy [File] [""]
    (listFiles []
      (into-array [(io/file "file-a")
                   (io/file "file-b")
                   (io/file "subdirectory")]))
    (isDirectory []
      true)))

(describe "request-handler.filesystem"
  (context "directory"
    (it "has a text/html content type"
      (let [{headers :headers} (handler/directory {:path "/"} (mock-directory))]
        (should= "text/html" (get headers "Content-Type"))))

    (it "lists the contents of the directory"
      (let [{body :body} (handler/directory {:path "/"} (mock-directory))]
        (should-contain (html [:a {:href "/file-a"} "file-a"]) body)
        (should-contain (html [:a {:href "/file-b"} "file-b"]) body)
        (should-contain (html [:a {:href "/subdirectory"} "subdirectory"]) body)))

    (it "paths to the files are relative to the request path"
      (let [{body :body} (handler/directory {:path "/dir"} (mock-directory))]
        (should-contain (html [:a {:href "/dir/file-a"} "file-a"]) body))))

  (describe "file handlers"
    (with file (File/createTempFile "http-clj-test-file-handler" ".txt"))
    (with test-path (.getPath @file))
    (with test-data "Some content")

    (before (spit @test-path @test-data))
    (after (.delete @file))

    (context "partial-file"
      (it "responds with a 206 if a range is provided"
        (let [request {:headers {:range {:start 0 :end 0}}}
              resp (handler/partial-file request @test-path)]
          (should= 206 (:status resp))))

      (it "has the content type of the file"
        (let [request {:headers {:range {:start 0 :end 0}}}
              resp (handler/partial-file request @test-path)]
          (should= "text/plain" (get-in resp [:headers :content-type]))))

      (it "it has the requested range in the body"
        (let [request {:headers {:range {:start 0 :end 3}}}
              resp (handler/partial-file request @test-path)]
          (should= "Some" (String. (:body resp))))

        (let [request {:headers {:range {:start 1 :end 3}}}
              resp (handler/partial-file request @test-path)]
          (should= "ome" (String. (:body resp)))))

      (it "responds with a 416 if the range is not satisfiable"
        (let [request {:headers {:range {:start 1 :end 500}}}
              resp (handler/partial-file request @test-path)]
          (should= 416 (:status resp)))))

    (context "patch-file"
      (with request {:body (.getBytes "New content")})

      (it "responds with 204 when successful"
        (let [resp (handler/patch-file @request  @test-path)]
          (should= 204 (:status resp))))

      (it "has no body"
        (let [resp (handler/patch-file @request @test-path)]
          (should= "" (:body resp))))

      (it "updates the contents of the file"
        (let [resp (handler/patch-file @request @test-path)]
          (should= "New content" (slurp @test-path))))

      (it "updates the contents of the file if the when the precondition is true"
        (let [request (assoc-in @request [:headers :if-match] (entity/tag @test-data))
              resp (handler/patch-file request @test-path)]
          (should= "New content" (slurp @test-path))))

      (it "responds with 409 when the precondition fails"
        (let [request (assoc-in @request [:headers :if-match] "abcdef09")
              resp (handler/patch-file request @test-path)]
          (should= 409 (:status resp))
          (should= @test-data (slurp @test-path))))

      (it "does not modify the file when the precondition fails"
        (let [request (assoc-in @request [:headers :if-match] "abcdef09")
              resp (handler/patch-file request @test-path)]
          (should= @test-data (slurp @test-path)))))

    (context "file"
      (it "can accept a request and a file object"
        (let [{message :body} (handler/file {} @test-path)]
          (should= (seq message) (seq (.getBytes @test-data)))))

      (it "the Content-Type header is included in the response"
        (let [{headers :headers} (handler/file {} @test-path)]
          (should= "text/plain" (:content-type headers))))

      (it "it uses partial-file if a range is provided"
        (let [request {:headers {:range {:start 0 :end 0}}}]
          (should-invoke handler/partial-file {:times 1}
                         (handler/file request @test-path)))))))
