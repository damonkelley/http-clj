(ns http-clj.request.parser.headers
  (:require [clojure.string :as string]))

(defn- lower-case-field-name [[field-name field-value]]
  [(string/lower-case field-name) field-value])

(defn- field-name->keyword [[field-name field-value]]
  [(keyword field-name) field-value])

(defn- filter-matching-groups [matches]
  (drop 1 matches))

(defn- trim-name-and-value [header]
  (map string/trim header))

(defn- split-header [header]
    (->> header
        (re-find #"^(.*?):(.*?)$")
        filter-matching-groups
        trim-name-and-value))

(defn- parse-field-name:field-value [header]
  (-> header
      split-header
      lower-case-field-name
      field-name->keyword))

(defn- split-range-header [field-value]
  (string/split field-value #"=|-"))

(defn- to-range-map [split-fields]
  (zipmap [:units :start :end] split-fields))

(defn- parse-byte-position [position]
  (try
    (Integer/parseInt position)
    (catch java.lang.NumberFormatException _ nil)))

(defn- parse-start-end [range-map]
  (-> range-map
      (update :start parse-byte-position)
      (update :end parse-byte-position)))

(defn- parse-range [field-value]
  (-> field-value
      split-range-header
      to-range-map
      parse-start-end))

(defn parse-field-value [headers field-name parser]
  (if-let [field-value (field-name headers)]
    (update headers field-name parser)
    headers))

(defn parse-field-values [headers]
  (-> headers
      (parse-field-value :content-length #(Integer/parseInt %))
      (parse-field-value :range parse-range)))

(defn parse [headers]
  (->> headers
       (map parse-field-name:field-value)
       (into {})
       parse-field-values))
