(ns http-clj.application.hello-world-spec
  (:require [speclj.core :refer :all]
            [clj-http.client :as client]
            [http-clj.server :as s]
            [http-clj.application.hello-world :refer [app]])
  (:import java.util.concurrent.CountDownLatch))

(defn new-latch []
  (CountDownLatch. 1))

(defn start-server [app port latch]
  (doto (Thread. #(s/serve (s/create port) {:entrypoint app} latch))
    (.start)))

(defn shutdown-server
  ([thread]
   (.interrupt thread)))

(describe "hello world"
  (around [it]
    (let [latch (new-latch)
          thread (start-server app 5000 latch)]
      (.await latch)
      (.interrupt thread)
      (it)))

  (it "accepts an HTTP request"
    (client/get "http://localhost:5000"))

  (it "contains 'Hello, world!'"
    (should= "Hello, world!", (:body (client/get "http://localhost:5000"))))

  (it "/foo is a valid path"
    (should= "Bar", (:body (client/get "http://localhost:5000/foo")))))
