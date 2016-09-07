(ns http-clj.request.parser.request-line
  (:require [clojure.string :as string])
  (:import java.net.URLDecoder))

(defn- split-request-line [line]
  (into [] (take 3 (concat (string/split line #" ") (repeat "")))))

(defn- split-query-string [query-string]
  (string/split query-string #"=|&"))

(defn- query-fields-to-map [query-fields]
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
         query-fields-to-map
         (assoc parsed-path :query-params))
    parsed-path))

(defn parse-path [request-line]
  (->> (:path request-line)
       split-path-and-query-string
       parse-query-string
       (merge request-line)))

(defn parse [request-line]
  (->> request-line
       split-request-line
       (zipmap [:method :path :version])
       parse-path))
