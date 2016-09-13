(ns http-clj.response.helpers-spec
  (:require [speclj.core :refer :all]
            [http-clj.response.helpers :as helpers]))

(describe "response.helpers"
  (describe "assoc-content-type"
    (it "adds the content-type to the response"
      (should= {:headers {:content-type "image/gif"}}
               (helpers/add-content-type {} "image.gif")))

    (it "does not add the header if the mimetype can not be determined"
      (should= {} (helpers/add-content-type {} "file")))))
