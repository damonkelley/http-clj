(ns http-clj.response.cookies-spec
  (:require [speclj.core :refer :all]
            [http-clj.response.cookies :as cookies]))

(describe "response.cookies"
  (describe "set-cookie"
    (it "adds the 'Set-Cookie' header to the response"
      (let [response {:headers {}}]
        (should-contain :set-cookie
                        (:headers (cookies/set-cookie response "key" "value")))))

    (it "adds a key value pair to the set-cookie header"
      (let [response {:headers {}}
            response (cookies/set-cookie response "name" "value")]

        (should= ["name=value"]
                 (get-in response [:headers :set-cookie]))))

    (it "updates the vector"
      (let [response (-> {:headers {:set-cookie ["name=value"]}}
                         (cookies/set-cookie "token" "abc123"))]

        (should= ["name=value" "token=abc123"]
                 (get-in response [:headers :set-cookie])))))

  (describe "format-cookie"
    (it "formats the pair into a string delimited by an equal sign"
      (should= "name=value" (cookies/format-cookie "name" "value"))
      (should= "token=abc123" (cookies/format-cookie "token" "abc123")))))
