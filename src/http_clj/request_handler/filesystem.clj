(ns http-clj.request-handler.filesystem
  (:require [http-clj.file :as f]
            [clojure.java.io :as io]
            [http-clj.response :as response]
            [http-clj.response.headers :as headers]
            [http-clj.presentation.template :as template]
            [http-clj.presentation.presenter :as presenter]
            [http-clj.entity :as entity]))

(defn directory [request dir]
  (let [files (presenter/files request (.listFiles dir))
        html (template/directory files)]
    (response/create request html :headers {"Content-Type" "text/html"})))

(defn- -partial-file [request path]
  (let [{:keys [start end units]} (get-in request [:headers :range])
        {:keys [start end length range]} (f/query-range path start end)]
    (-> request
      (response/create range :status 206)
      (headers/add-content-type path)
      (headers/add-content-range units start end length))))

(defn- range-unsatisfiable [request length]
  (let [units (get-in request [:headers :range :units])]
    (-> request
        (response/create "" :status 416)
        (headers/add-content-range units length))))

(defn partial-file [request path]
  (let [{start :start end :end} (get-in request [:headers :range])]
    (try
      (-partial-file request path)
      (catch clojure.lang.ExceptionInfo e
        (range-unsatisfiable request (:length (ex-data e)))))))

(defn- -file [request path]
  (-> request
      (response/create (f/binary-slurp path))
      (headers/add-content-type path)))

(defn file [{:keys [headers] :as request} path]
  (if (not-empty (:range headers))
    (partial-file request path)
    (-file request path)))

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
