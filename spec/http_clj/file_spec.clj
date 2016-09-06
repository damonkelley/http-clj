(ns http-clj.file-spec
  (:require [speclj.core :refer :all]
            [http-clj.file :as file]))

(describe "file"
  (with test-path "/tmp/http-clj-test-file")
  (before (spit @test-path ""))

  (context "binary-slurp"
    (with contents "the quick brown fox")
    (before (spit @test-path @contents))

    (it "reads the file contents into a byte-array"
      (should= (seq (.getBytes @contents))
               (seq (file/binary-slurp @test-path)))))

  (context "file-helper"
    (it "takes two paths"
      (file/resolve "resources/static" "image.gif"))

    (it "it forms the path correctly"
      (should= "resources/static/image.gif"
               (.getPath (file/resolve "resources/static/" "/image.gif"))))

    (it "returns a file at the requsted path"
      (should= "resources/static/image.gif"
               (.getPath (file/resolve "resources/static" "image.gif"))))))
