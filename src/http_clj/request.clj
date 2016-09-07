(ns http-clj.request
  (:require [clojure.string :as string]
            [http-clj.request.parser.headers :as headers]
            [http-clj.request.parser.request-line :as request-line]
            [http-clj.request.reader :as reader]
            [http-clj.request.validator :as validator]))

(defn get-request-line [request]
  (->> request
       reader/readline
       request-line/parse))

(defn get-headers [request]
  (-> request
      reader/read-headers
      headers/parse))

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
