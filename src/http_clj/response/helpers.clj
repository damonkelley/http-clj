(ns http-clj.response.helpers
  (:require [http-clj.file :as file]))

(defn add-content-type [resp path]
  (if-let [content-type (file/content-type-of path)]
    (assoc-in resp [:headers :content-type] content-type)
    resp))
