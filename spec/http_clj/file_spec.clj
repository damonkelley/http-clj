(ns http-clj.file-spec
  (:require [speclj.core :refer :all]
            [http-clj.file :as file]))

(describe "binary-slurp"
  (with test-path "/tmp/http-clj-test-file")
  (before (spit @test-path ""))

  (it "reads the file contents into a byte-array"
    (let [data "this quick brown fox"]
    (spit @test-path data)
    (should=
      (seq (.getBytes data))
      (seq (file/binary-slurp @test-path))))))
