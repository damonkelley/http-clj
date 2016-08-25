(ns http-clj.app.cob-spec.cli-spec
  (:require [speclj.core :refer :all]
            [http-clj.app.cob-spec.cli :refer :all]
            [clojure.java.io :as io]))

(describe "cli"
  (it "accepts a valid port"
    (should= 65535 (get-in (cli ["-p65535"]) [:options :port])))

  (it "will fallback to the default port if the provided port is invalid"
    (should-contain "Must be a number between 0 and 65536" (first (:errors (cli ["-p9000000"]))))
    (should= 5000 (get-in (cli ["-p900000"]) [:options :port])))

  (it "accepts a directory"
    (should= "resources/static/" (get-in (cli ["-dresources/static/"]) [:options :directory])))

  (it "defaults to public"
    (should= "./public" (get-in (cli []) [:options :directory])))

  (it "verifies the directory exists"
    (should-contain "Must be a valid directory" (first (:errors (cli ["-ddoes-not-exist"]))))))
