(ns http-clj.request-handler
  (:require [http-clj.file :as f]
            [http-clj.response :as response]))

(defn not-found [request]
  (response/create request "Not Found" :status 404))

(defn file [path]
  (fn [request]
    (response/create request (f/binary-slurp path))))
