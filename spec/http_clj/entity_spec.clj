(ns http-clj.entity-spec
  (:require [speclj.core :refer :all]
            [http-clj.entity :as entity]
            [digest :refer [sha1]]
            [clojure.java.io :as io]))

(describe "entity"
  (context "tag"
    (with file "/tmp/http-clj-digest-file")
    (with content "Test content")
    (before (spit @file @content))

    (it "uses digest.sha1"
      (should-invoke sha1 {:times 1 :with ["content"]}
                     (entity/tag "content")))

    (it "returns the digest"
      (should= "5e9b60f69165f32f8930843ca718e10fdee30c52" (entity/tag "tag"))
      (should= "040f06fd774092478d450774f5ba30c5da78acc8" (entity/tag "content")))

    (it "accepts a File"
      (let [file "/tmp/http-clj-digest-file"
            content "Test content"]
        (spit file content)
        (should= (sha1 "Test content") (entity/tag (io/as-file file)))))))
