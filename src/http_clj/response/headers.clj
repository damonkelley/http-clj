(ns http-clj.response.headers
  (:require [http-clj.file :as file]))

(defn add-content-type [resp path]
  (if-let [content-type (file/content-type-of path)]
    (assoc-in resp [:headers :content-type] content-type)
    resp))

(defn- format-content-range [units start end length]
  (str units " " start "-" end "/" length))

(defn- format-wildcard-content-range [units length]
  (str "bytes " "*/" length))

(defn- -add-content-range [resp field-value]
  (assoc-in resp [:headers :content-range] field-value))

(defn add-content-range
  ([resp units start end length]
  (-add-content-range resp (format-content-range units start end length)))
  ([resp units length]
   (-add-content-range resp (format-wildcard-content-range units length))))
