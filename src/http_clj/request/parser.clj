(ns http-clj.request.parser
  (:require [http-clj.connection :as connection]
            [clojure.string :as string]))

(defn- split-request-line [conn]
  (-> conn
      (connection/readline)
      (string/split #" ")))

(defn request-line [conn]
  (->> conn
       split-request-line
       (zipmap [:method :path :version])))

(defn- parse-header [header]
  (let [[field-name field-value] (string/split header #":")]
    {(string/trim field-name)
     (string/trim field-value)}))

(defn headers [conn]
  (loop [conn conn headers {}]
    (let [header (connection/readline conn)]
      (if (empty? header)
        headers
        (recur conn (merge headers (parse-header header)))))))
