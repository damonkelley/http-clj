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

(defn- split-range-header [field-value]
  (string/split field-value #"=|-"))

(defn- to-range-map [split-fields]
  (zipmap [:units :start :end] split-fields))

(defn- parse-start-end [range-map]
  (-> range-map
      (update :start #(Integer/parseInt %))
      (update :end #(Integer/parseInt %))))

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

(defn parse-headers [headers]
  (->> headers
       (map parse-field-name:field-value)
       (into {})
       parse-field-values))
