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

(defn new-latch []
  (CountDownLatch. 1))

(defn start-server [app port]
  (let [latch (new-latch)
        thread (Thread. #(s/serve (s/create port) app latch))]
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

(describe "fallback"
  (it "responds with a listing if directory"
    (should-contain "image.gif" (:body (fallback {:path "/"}))))

  (it "responds with a file if it is a file"
    (should-contain "File contents" (String. (:body (fallback {:path "/file.txt"})))))

  (it "responds with 404 if a file is not found"
    (should= 404 (:status (fallback {:path "/file-that-does-not-exist.txt"})))))

(describe "file-helper"
  (it "takes two paths"
    (file-helper "resources/static" "image.gif"))
  (it "it forms the path correctly"
    (should= "resources/static/image.gif" (.getPath (file-helper "resources/static/" "/image.gif"))))
  (it "returns a file at the requsted path"
    (should= "resources/static/image.gif" (.getPath (file-helper "resources/static" "image.gif")))))
