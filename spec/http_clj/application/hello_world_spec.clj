(ns http-clj.application.hello-world-spec
  (:require [speclj.core :refer :all]
            [clj-http.client :as client]
            [http-clj.spec.integration :as integration]
            [http-clj.mock :as mock]
            [http-clj.application.hello-world :refer [app]]))


(describe "hello world"
  (around [it]
    (let [latch (integration/new-latch)
          thread (integration/start-server app 5000 latch)]
      (.await latch)
      (.interrupt thread)
      (it)))

  (it "accepts an HTTP request"
    (client/get "http://localhost:5000"))

  (it "contains 'Hello, world!'"
    (should= "Hello, world!\r\n", (:body (client/get "http://localhost:5000")))))
