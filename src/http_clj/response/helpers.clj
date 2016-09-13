(ns http-clj.response.helpers
  (:require [http-clj.file :as file]))

(defn add-content-type [resp path]
  (if-let [content-type (file/content-type-of path)]
    (assoc-in resp [:headers :content-type] content-type)
    resp))

(defn- format-content-range [units start end length]
  (str units " " start "-" end "/" length))

(defn add-content-range [resp units start end length]
  (assoc-in resp [:headers :content-range]
            (format-content-range units start end length)))
