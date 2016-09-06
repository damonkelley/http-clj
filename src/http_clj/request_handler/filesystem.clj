(ns http-clj.request-handler.filesystem
  (:require [http-clj.file :as f]
            [http-clj.response :as response]
            [http-clj.presentation.template :as template]
            [http-clj.presentation.presenter :as presenter]
            [digest :refer [sha1]]))

(defn directory [request dir]
  (let [files (presenter/files request (.listFiles dir))
        html (template/directory files)]
    (response/create request html :headers {"Content-Type" "text/html"})))

(defn partial-file [request path]
  (let [{start :start end :end} (get-in request [:headers :range])]
    (try
      (response/create request (f/binary-slurp-range path start end) :status 206)
      (catch clojure.lang.ExceptionInfo e
        (response/create request "" :status 416)))))

(defn- -patch-file [request file]
  (with-open [stream (clojure.java.io/output-stream file)]
    (.write stream (:body request)))
  (response/create request "" :status 204))

(defn- conflict [request]
  (response/create request "" :status 409))

(defn patch-file [{:keys [headers] :as request} file]
  (let [precondition (:if-match headers)
        content (f/binary-slurp file)]
    (cond (nil? precondition) (-patch-file request file)
          (= precondition (sha1 content)) (-patch-file request file)
          :else (conflict request))))

(defn file [{:keys [headers] :as request} path]
  (if (not-empty (:range headers))
      (partial-file request path)
      (response/create request (f/binary-slurp path))))
