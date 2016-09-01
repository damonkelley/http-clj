(ns http-clj.request.parser
  (:require [http-clj.connection :as connection]
            [clojure.string :as string]))

(defn- continue-reading? [-byte]
  (some #(= -byte %) (map int [\newline -1])))

(defn- remove-carriage-returns [-bytes]
  (remove #(= (int \return) %) -bytes))

(defn- convert-to-string [-bytes]
  (-> -bytes
      remove-carriage-returns
      byte-array
      String.))

(defn readline [conn]
    (loop [-bytes []]
      (let [-byte (connection/read-byte conn)]
        (if (continue-reading? -byte)
          (convert-to-string -bytes)
          (recur (conj -bytes -byte))))))

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
