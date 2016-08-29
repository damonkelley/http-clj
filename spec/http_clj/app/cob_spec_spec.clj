(ns http-clj.app.cob-spec-spec (:require [speclj.core :refer :all]
            [clj-http.client :as client]
            [http-clj.server :as s]
            [http-clj.file :as file]
            [http-clj.request-handler :as handler]
            [http-clj.app.cob-spec :refer :all]
            [clojure.java.io :as io])
  (:import java.util.concurrent.CountDownLatch
           java.io.File))

(def root "http://localhost:5000")

(defn start-server [app port]
  (let [latch (CountDownLatch. 1)
        server #(s/serve (s/create (app "resources/static/")
                                   :port port
                                   :latch latch))
        thread (Thread. server)]
    (.start thread)
    (.await latch)
    thread))

(defn shutdown-server [thread]
  (.interrupt thread)
  (client/get root))

(defn GET [path]
  (client/get (str root path) {:throw-exceptions false}))

(def thread (atom nil))

(describe "cob-spec"
  (before-all (reset! thread (start-server app 5000)))
  (after-all (shutdown-server @thread))
  (it "has /"
    (let [{status :status body :body} (GET "/")]
      (should= 200 status)
      (should-contain "file.txt" body)
      (should-contain "image.gif" body)))

    (it "has /image.gif"
      (should= 200 (:status (GET "/image.gif"))))

    (it "has a viewable log"
      (should-contain "GET /log HTTP/1.1" (:body (GET "/log")))))
