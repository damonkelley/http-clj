(ns http-clj.request-handler.filesystem-spec
  (:require [speclj.core :refer :all]
            [http-clj.request-handler.filesystem :as handler]
            [http-clj.response :as response]
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
    (with test-path "/tmp/http-clj-test-file-handler")
    (with test-data "Some content")
    (before-all (spit @test-path @test-data))

    (context "partial-file"
      (it "responds with a 206 if a range is provided"
        (let [request {:headers {:range {:start 0 :end 0}}}
              resp (handler/partial-file request @test-path)]
          (should= 206 (:status resp))))

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

    (context "file"
      (it "can accept a request and a file object"
        (let [{message :body} (handler/file {} @test-path)]
          (should= (seq message) (seq (.getBytes @test-data)))))

      (it "it uses partial-file if a range is provided"
        (let [request {:headers {:range {:start 0 :end 0}}}]
          (should-invoke handler/partial-file {:times 1}
                         (handler/file request @test-path)))))))
