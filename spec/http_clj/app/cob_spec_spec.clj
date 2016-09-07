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

(defn stop-listening []
  (try
    (client/get root)
    (catch java.net.ConnectException e
      (println "Server already shutdown"))))

(defn shutdown-server [thread]
  (.interrupt thread)
  (when (.isAlive thread)
    (stop-listening)))

(defn GET [path]
  (client/get (str root path) {:throw-exceptions false}))

(defn POST [path data]
  (client/post (str root path) {:form-params data
                                :throw-exceptions false}))

(defn OPTIONS [path]
  (client/options (str root path) {:throw-exceptions false}))

(def thread (atom nil))

(describe "cob-spec"
  (before-all (reset! thread (start-server app 5000)))
  (after-all (shutdown-server @thread))
  (it "has /"
    (let [{status :status body :body} (GET "/")]
      (should= 200 status)
      (should-contain "file.txt" body)
      (should-contain "image.gif" body)))

  (it "will attempt to patch a static file"
    (let [resp (client/patch
                 "http:localhost:5000/file.txt"
                 {:headers {:if-match "incorrect-etag"}
                  :throw-exceptions false})]
      (should= 409 (:status resp))))

  (it "has /image.gif"
    (should= 200 (:status (GET "/image.gif"))))

  (it "can get partial contents of file.txt"
    (let [resp (client/get "http://localhost:5000/file.txt"
                           {:headers {:range "bytes=0-4"}})]
      (should= 206 (:status resp))))

  (it "has a viewable log when authenticated"
    (let [response (client/get
                     "http://localhost:5000/logs"
                     {:basic-auth ["admin" "hunter2"]})]
      (should-contain "GET /logs HTTP/1.1" (:body response))))

  (it "unauthorized access to the log is not allowed"
    (let [response (GET "/logs")]
      (should= 401 (:status response))))

  (it "data can be posted to /form"
    (let [{status :status} (POST "/form" {:form-data true})]
      (should= 200 status))
    (should-contain "form-data=true" (:body (GET "/form")))
    (POST "/form" {:new-form-data true})
    (should-contain "new-form-data=true" (:body (GET "/form"))))

  (it "shows the options at /method_options"
    (let [headers (:headers (OPTIONS "/method_options"))]
      (should= "GET,HEAD,POST,OPTIONS,PUT"(get headers "Allow"))))

  (it "shows the options at /method_options2"
    (let [headers (:headers (OPTIONS "/method_options2"))]
      (should= "GET,OPTIONS" (get headers "Allow"))))

  (it "presents the decoded and formatted query parameters"
    (let [resp (client/get
                 (str root "/parameters")
                 {:query-params {"key" "value"}})]
      (should= "key = value" (:body resp)))))
