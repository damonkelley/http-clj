(ns http-clj.handler
  (:require [http-clj.file :as f]
            [http-clj.response :as response]))

(defn file [path]
  (fn [request]
    (response/create request (f/binary-slurp path))))
