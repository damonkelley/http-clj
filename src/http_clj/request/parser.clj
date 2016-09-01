(ns http-clj.request.parser
  (:require [http-clj.connection :as connection]
            [http-clj.request.reader :as reader]
            [clojure.string :as string]))

(defn parse-request-line [request-line]
  (-> request-line
      (string/split #" ")
      (#(zipmap [:method :path :version] %))))

(defn- parse-field:value [header]
  (let [[field-name field-value] (string/split header #":")]
    {(string/trim field-name)
     (string/trim field-value)}))

(defn parse-header-fields [headers]
  (if-let [content-length (get headers "Content-Length")]
    (update headers "Content-Length" #(Integer/parseInt %))
    headers))

(defn parse-headers [headers]
  (->> headers
      (map parse-field:value)
      (into {})
      parse-header-fields))
