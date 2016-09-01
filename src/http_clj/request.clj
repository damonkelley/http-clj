(ns http-clj.request
  (:require [clojure.string :as string]
            [http-clj.request.parser :as parser]
            [http-clj.request.reader :as reader]
            [http-clj.request.validator :as validator]))

(defn get-request-line [request]
  (->> request
       reader/readline
       parser/parse-request-line))

(defn get-headers [request]
  (-> request
      reader/read-headers
      parser/parse-headers))

(defn- attach-request-line [request]
  (merge request (get-request-line request)))

(defn- attach-headers [request]
  (assoc request :headers (get-headers request)))

(defn- attach-body [request]
  (assoc request :body (reader/read-body request)))

(defn create [conn]
   (-> {:conn conn}
       attach-request-line
       attach-headers
       attach-body
       validator/validate))
