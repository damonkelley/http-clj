(ns http-clj.application.cob-spec.handlers-spec
  (:require [speclj.core :refer :all]
            [http-clj.application.cob-spec.handlers :refer :all]))

(describe "fallback"
  (with dir "resources/static/")
  (it "responds with a listing if directory"
    (should-contain "image.gif" (:body (fallback {:path "/"} @dir))))

  (it "responds with a file if it is a file"
    (should-contain "File contents" (String. (:body (fallback {:path "/file.txt"} @dir)))))

  (it "responds with 404 if a file is not found"
    (should= 404 (:status (fallback {:path "/file-that-does-not-exist.txt"} @dir)))))
