(ns http-clj.file-spec
  (:require [speclj.core :refer :all]
            [http-clj.file :as file]))

(describe "file"
  (context "binary-slurp"
    (with test-path "/tmp/http-clj-test-file")
    (before (spit @test-path ""))

    (it "reads the file contents into a byte-array"
      (let [data "this quick brown fox"]
        (spit @test-path data)
        (should=
          (seq (.getBytes data))
          (seq (file/binary-slurp @test-path))))))

  (context "file-helper"
    (it "takes two paths"
      (file/resolve "resources/static" "image.gif"))

    (it "it forms the path correctly"
      (should= "resources/static/image.gif" (.getPath (file/resolve "resources/static/" "/image.gif"))))

    (it "returns a file at the requsted path"
      (should= "resources/static/image.gif" (.getPath (file/resolve "resources/static" "image.gif"))))))
