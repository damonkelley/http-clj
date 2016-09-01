(ns http-clj.response
  (:require [clojure.string :as string]
            [http-clj.connection :as connection]))

(defn create [request body & {:keys [status headers]
                              :or {status 200
                                   headers {}}}]
  {:body body
   :headers headers
   :status status
   :conn (:conn request)})

(def reasons
  {200 "OK"
   404 "Not Found"})

(def CRLF "\r\n")

(defn- reason-for [status]
  (get reasons status))

(defn- to-byte-array [string]
  (byte-array (map (comp byte int) string)))

(defn- format-header [[field-name field-value]]
  (str field-name ": " field-value CRLF))

(defn- format-headers [headers]
  (apply str (map format-header headers)))

(defn- generate-status-line [status]
  (let [version "HTTP/1.1"
        status status
        reason (reason-for status)]
    (str
      (string/join " " [version status reason])
      CRLF)))

(defn- generate-message [status headers body]
  (->> [(generate-status-line status) (format-headers headers) CRLF body]
       (map to-byte-array)
       (mapcat seq)
       (byte-array)))

(defn compose [resp]
  (let [{status :status headers :headers body :body} resp]
    (assoc resp :message (generate-message status headers body))))

(defn write [resp]
  (->> resp
      compose
      :message
      (connection/write (:conn resp))))
