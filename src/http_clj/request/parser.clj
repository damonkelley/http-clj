(ns http-clj.request.parser
  (:require [http-clj.connection :as connection]
            [http-clj.request.reader :as reader]
            [clojure.string :as string])
  (:import java.net.URLDecoder))

(defn- split-request-line [line]
  (into [] (take 3 (concat (string/split line #" ") (repeat "")))))

(defn- split-query-string [query-string]
  (string/split query-string #"=|&"))

(defn- -query-fields-to-map [query-fields]
  (reduce
    (fn [acc [key val]] (assoc acc key val))
    {}
    (partition 2 query-fields)))

(defn- split-path-and-query-string [raw-path]
  (zipmap
    [:path :query-params]
    (take 2 (concat (string/split raw-path #"\?") (repeat nil)))))

(defn- decode-fields [query-fields]
  (map #(URLDecoder/decode %) query-fields))

(defn- parse-query-string [{:keys [query-params] :as parsed-path}]
  (if query-params
    (->> query-params
         split-query-string
         decode-fields
         -query-fields-to-map
         (assoc parsed-path :query-params))
    parsed-path))

(defn parse-path [request-line]
  (->> (:path request-line)
       split-path-and-query-string
       parse-query-string
       (merge request-line)))

(defn parse-request-line [request-line]
  (->> request-line
       split-request-line
       (zipmap [:method :path :version])
       parse-path))

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

(defn parse-headers [headers]
  (->> headers
       (map parse-field-name:field-value)
       (into {})
       parse-field-values))
