(ns http-clj.presentation.presenter-spec
  (:require [speclj.core :refer :all]
            [http-clj.presentation.presenter :as presenter]
            [clojure.java.io :as io]))

(describe "presenter"
  (context "file"
    (with file (io/file "file"))
    (it "has the href to the file"
      (should= "/file" (:href (presenter/file {:path "/"} @file))))
    (it "uses the path in the request to construct the href"
      (should= "/path/to/file" (:href (presenter/file {:path "/path/to"} @file))))
    (it "can construct the href to a file with a relative path"
      (should= "/path/to/file" (:href (presenter/file {:path "/path/to/"} (io/file "../relative/to/file")))))
    (it "has the name of the file"
      (should= "file" (:name (presenter/file {:path "/"} @file)))))

  (context "files"
    (with files [(io/file "file-a")
                 (io/file "file-b")])
    (it "presents a collection of files"
      (let [[file-a file-b] (presenter/files {:path "/"} @files)]
        (should= {:href "/file-a" :name "file-a"} file-a)
        (should= {:href "/file-b" :name "file-b"} file-b)))

    (it "the href to the files are relative to the request path"
      (let [[file-a file-b] (presenter/files {:path "/path/"} @files)]
        (should= "/path/file-a" (:href file-a))))))
