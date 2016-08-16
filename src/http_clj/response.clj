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

(defn reason-for [status]
  (get reasons status))

(defn compose-status-line [status]
  (let [version "HTTP/1.1"
        status status
        reason (reason-for status)]
  (string/join " " [version status reason])))

(defn compose [resp]
  (let [{status :status body :body} resp]
  (assoc resp :message (str (compose-status-line status) "\r\n"
                            "\r\n"
                            body))))
