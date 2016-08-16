(ns http-clj.request
  (:require [http-clj.connection :as connection]
            [clojure.string :as string]))

(defn- split-request-line [conn]
  (-> conn
      (connection/readline)
      (string/split #" ")))

(defn parse-request-line [conn]
  (->> conn
       split-request-line
       (zipmap [:method :path :version])))

(defn- parse-header [header]
  (let [[field-name field-value] (string/split header #":")]
    {(string/trim field-name)
     (string/trim field-value)}))

(defn parse-headers [conn]
  (loop [conn conn headers {}]
    (let [header (connection/readline conn)]
      (if (empty? header)
        headers
        (recur conn (merge headers (parse-header header)))))))

(defn- attach-request-line [request]
  (merge request (parse-request-line (:conn request))))

(defn- attach-headers [request]
  (assoc request :headers (parse-headers (:conn request))))

(defn create [conn]
  (-> {:conn conn}
      attach-request-line
      attach-headers))
