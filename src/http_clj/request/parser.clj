(ns http-clj.request.parser
  (:require [http-clj.connection :as connection]
            [http-clj.request.reader :as reader]
            [clojure.string :as string]))

(defn- split-request-line [line]
  (take 3 (concat (string/split line #" ") (repeat ""))))

(defn parse-request-line [request-line]
  (->> request-line
       split-request-line
       (zipmap [:method :path :version])))

(defn- lower-case-field-name [[field-name field-value]]
  [(string/lower-case field-name) field-value])

(defn- field-name->keyword [[field-name field-value]]
  [(keyword field-name) field-value])

(defn- split-header [header]
  (map string/trim (string/split header #":")))

(defn- parse-field-name:field-value [header]
  (-> header
      split-header
      lower-case-field-name
      field-name->keyword))

(defn parse-field-values [headers]
  (if-let [content-length (get headers :content-length)]
    (update headers :content-length #(Integer/parseInt %))
    headers))

(defn parse-headers [headers]
  (->> headers
       (map parse-field-name:field-value)
       (into {})
       parse-field-values))
