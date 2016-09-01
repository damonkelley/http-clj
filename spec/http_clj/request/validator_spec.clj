(ns http-clj.request.validator-spec
  (:require [speclj.core :refer :all]
            [http-clj.request.validator :refer :all]))

(describe "request.validator"
  (context "validate"
    (it "determines the request is invalid if the method is empty"
      (let [request {:method "" :path "/" :version "HTTP/1.1"}]
        (should= (assoc request :valid? false)
                 (validate request))))

    (it "determines the request is invalid if the path is empty"
      (let [request {:method "GET" :path "" :version "HTTP/1.1"}]
        (should= (assoc request :valid? false)
                 (validate request))))
    (it "deems all other requests as valid"
      (let [request {:method "GET" :path "/" :version "HTTP/1.1"}]
        (should= (assoc request :valid? true)
                 (validate request))))))
