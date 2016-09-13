(ns http-clj.response.headers-spec
  (:require [speclj.core :refer :all]
            [http-clj.response.headers :as headers]))

(describe "response.headers"
  (describe "add-content-type"
    (it "adds the content-type to the response"
      (should= {:headers {:content-type "image/gif"}}
               (headers/add-content-type {} "image.gif")))

    (it "does not add the header if the mimetype can not be determined"
      (should= {} (headers/add-content-type {} "file"))))

  (describe "add-content-range"
    (it "adds the content-range to the response"
      (should= {:headers {:content-range "bytes 0-3/77"}}
               (headers/add-content-range {} "bytes" 0 3 77))

      (should= {:headers {:content-range "chars 6-10/100"}}
               (headers/add-content-range {} "chars" 6 10 100)))

    (it "provides a wildcard range when only the units and length are given"
      (should= {:headers {:content-range "bytes */100"}}
               (headers/add-content-range {} "bytes" 100)))))
