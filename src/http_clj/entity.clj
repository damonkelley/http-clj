(ns http-clj.entity
  (:require [digest :refer [sha1]]))

(defn tag [content]
  (sha1 content))
