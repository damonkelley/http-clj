(ns http-clj.request.parser
  (:require [http-clj.connection :as connection]
            [clojure.string :as string]))

(defn readline [conn]
    (loop [line []]
      (let [character (connection/read-byte conn)]
        (if (some #(= character %) (map int [\newline -1]))
          (String. (byte-array (filter #(not= (int \return) %) line)))
          (recur (conj line character))))))

(defn- split-request-line [conn]
  (-> conn
      (readline)
      (string/split #" ")))

(defn request-line [request]
  (->> (:conn request)
       split-request-line
       (zipmap [:method :path :version])))

(defn- parse-header [header]
  (let [[field-name field-value] (string/split header #":")]
    {(string/trim field-name)
     (string/trim field-value)}))

(defn headers [request]
  (loop [conn (:conn request) headers {}]
    (let [header (readline conn)]
      (if (empty? header)
        headers
        (recur conn (merge headers (parse-header header)))))))

(defn read-body [{:keys [headers conn]}]
  (if-let [content-length (get headers "Content-Length")]
    (connection/read-bytes conn (Integer/parseInt content-length))))
