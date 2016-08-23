(ns http-clj.application.cob-spec-spec
  (:require [speclj.core :refer :all]
            [clj-http.client :as client]
            [http-clj.server :as s]
            [http-clj.file :as file]
            [http-clj.request-handler :as handler]
            [http-clj.application.cob-spec :refer :all]
            [clojure.java.io :as io])
  (:import java.util.concurrent.CountDownLatch
           java.io.File))

(def root "http://localhost:5000")

(defn start-server [app port]
  (let [latch (CountDownLatch. 1)
        application (app "resources/static/")
        thread (Thread. #(s/serve (s/create port) application latch))]
    (.start thread)
    (.await latch)
    thread))

(defn shutdown-server [thread]
  (.interrupt thread)
  (client/get root))

(def thread (start-server app 5000))

(defn GET [path]
  (client/get (str root path) {:throw-exceptions false}))

(describe "cob-spec"
  (after-all (shutdown-server thread))
  (it "has /"
    (let [{status :status body :body} (GET "/")]
      (should= 200 status)
      (should-contain "file.txt" body)
      (should-contain "image.gif" body)))

  (context "it can serve images"
    (it "has /image.gif"
      (should= 200 (:status (GET "/image.gif"))))))

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

(describe "fallback"
  (with dir "resources/static/")
  (it "responds with a listing if directory"
    (should-contain "image.gif" (:body (fallback {:path "/"} @dir))))

  (it "responds with a file if it is a file"
    (should-contain "File contents" (String. (:body (fallback {:path "/file.txt"} @dir)))))

  (it "responds with 404 if a file is not found"
    (should= 404 (:status (fallback {:path "/file-that-does-not-exist.txt"} @dir)))))
