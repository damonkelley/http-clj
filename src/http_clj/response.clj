(ns http-clj.response
  (:require [clojure.string :as string]))

(defn create [request body & {:keys [status]
                              :or {status 200}}]
  {:body body
   :status status
   :conn (:conn request)})

(def reasons
  {200 "OK"
   404 "Not Found"})

(defn- reason-for [status]
  (get reasons status))

(defn- to-byte-array [string]
  (byte-array (map (comp byte int) string)))

(defn- generate-status-line [status]
  (let [version "HTTP/1.1"
        status status
        reason (reason-for status)]
    (str
      (string/join " " [version status reason])
      "\r\n")))

(defn- generate-message [status body]
  (->> [(generate-status-line status) "\r\n" body]
      (map to-byte-array)
      (mapcat seq)
      (byte-array)))

(defn compose [resp]
  (let [{status :status body :body} resp]
    (assoc resp :message (generate-message status body))))
