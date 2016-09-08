(ns http-clj.request-handler-spec
  (:require [speclj.core :refer :all]
            [http-clj.request-handler :as handler]
            [http-clj.response :as response]
            [clojure.java.io :as io]
            [clojure.data.codec.base64 :as b64]
            [hiccup.core :refer [html]])
  (:import java.io.File))


(defn mock-directory []
  (proxy [File] [""]
    (listFiles []
      (into-array [(io/file "file-a")
                   (io/file "file-b")
                   (io/file "subdirectory")]))
    (isDirectory []
      true)))

(describe "handler"
  (context "not-found"
    (it "responds with status code 404"
      (should= 404 (:status (handler/not-found {}))))

    (it "has Not Found in the body"
      (should= "Not Found" (:body (handler/not-found {})))))

  (context "method-not-allowed"
    (it "responds with status code 405"
      (should= 405 (:status (handler/method-not-allowed {}))))

    (it "has Method Not Allowed in the body"
      (should= "Method Not Allowed" (:body (handler/method-not-allowed {})))))

  (context "head"
    (it "returns a resp with the body stripped out"
      (let [handler #(response/create % "Body" :status 200)
            resp (handler/head handler {})]
        (should= nil (:body resp))))

    (it "keeps the headers"
      (let [handler #(response/create % "Body" :headers {"Host" "www.example.com"})
            resp (handler/head handler {})]
        (should= {"Host" "www.example.com"} (:headers resp))
        (should= 200 (:status resp))))

    (it "keeps the status"
      (let [handler #(response/create % "Body" :status 201 :headers {"User-Agent" "test-agent"})
            resp (handler/head handler {})]
        (should= {"User-Agent" "test-agent"} (:headers resp)))))

  (context "auth"
    (with handler #(response/create % "Welcome"))

    (it "returns a 401 if the credentials are not provided"
      (let [handler (handler/auth @handler "username" "password")
            resp (handler {})]
        (should= 401 (:status resp))))

    (it "has the www-auth header when auth fails"
      (let [credentials (String. (b64/encode (.getBytes "admin:admin")))
            handler (handler/auth @handler "username" "password")
            resp (handler {:headers {:authorization (str "Basic " credentials)}})]
        (should= "Basic realm=\"simple\"" (get-in resp [:headers :www-authenticate]))))

    (it "dispatches to the handler if authentication is successful"
      (let [credentials (String. (b64/encode (.getBytes "admin:password")))
            handler (handler/auth @handler "admin" "password")
            resp (handler {:headers {:authorization (str "Basic " credentials)}})]
        (should= 200 (:status resp))
        (should= "Welcome" (:body resp)))))

  (context "redirect"
    (it "responds with a 302 status code"
      (should= 302 (:status (handler/redirect {} "/"))))

    (it "has a location header"
      (let [resp (handler/redirect {} "/path")]
        (should= "/path" (get-in resp [:headers :location]))))))
