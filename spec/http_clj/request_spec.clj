(ns http-clj.request-spec
  (:require [speclj.core :refer :all]
            [http-clj.request :as request]
            [http-clj.mock :as mock]
            [clojure.java.io :as io]))

(defn compose-input [& args]
  (clojure.string/join "" (interleave args (repeat "\r\n"))))

(describe "create"
  (it "reads the connection input"
    (should= ["Line 1"] (-> (compose-input "Line 1")
                            (mock/connection)
                            (request/create)
                            (:input)))

    (should= ["Line 1" "Line 2"] (-> (compose-input "Line 1" "Line 2")
                                     (mock/connection)
                                     (request/create)
                                     (:input))))

  (it "attaches the connection to the request"
    (let [conn (mock/connection)]
    (should= conn (:conn (request/create conn))))))
