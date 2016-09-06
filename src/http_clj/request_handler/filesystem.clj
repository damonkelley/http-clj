(ns http-clj.request-handler.filesystem
  (:require [http-clj.file :as f]
            [clojure.java.io :as io]
            [http-clj.response :as response]
            [http-clj.presentation.template :as template]
            [http-clj.presentation.presenter :as presenter]
            [http-clj.entity :as entity]))

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

(defn- if-match? [precondition file]
  (= precondition (entity/tag (io/as-file file))))

(defn patch-file [{:keys [headers] :as request} file]
  (let [precondition (:if-match headers)]
    (cond (nil? precondition) (-patch-file request file)
          (if-match? precondition file) (-patch-file request file)
          :else (conflict request))))

(defn file [{:keys [headers] :as request} path]
  (if (not-empty (:range headers))
      (partial-file request path)
      (response/create request (f/binary-slurp path))))
