(ns http-clj.response.helpers-spec
  (:require [speclj.core :refer :all]
            [http-clj.response.helpers :as helpers]))

(describe "response.helpers"
  (describe "add-content-type"
    (it "adds the content-type to the response"
      (should= {:headers {:content-type "image/gif"}}
               (helpers/add-content-type {} "image.gif")))

    (it "does not add the header if the mimetype can not be determined"
      (should= {} (helpers/add-content-type {} "file"))))

  (describe "add-content-range"
    (it "adds the content-range to the response"
      (should= {:headers {:content-range "bytes 0-3/77"}}
               (helpers/add-content-range {} "bytes" 0 3 77))

      (should= {:headers {:content-range "chars 6-10/100"}}
               (helpers/add-content-range {} "chars" 6 10 100)))

    (it "provides a wildcard range when only the units and length are given"
      (should= {:headers {:content-range "bytes */100"}}
               (helpers/add-content-range {} "bytes" 100)))))
