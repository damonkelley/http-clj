(ns http-clj.request
  (:require [http-clj.connection :as connection]
            [http-clj.logging :as logging]
            [clojure.string :as string]
            [http-clj.request.parser :as parser]
            [http-clj.request.reader :as reader]))

(defn get-request-line [request]
  (->> request
       reader/readline
       parser/parse-request-line))

(defn- attach-request-line [request]
  (merge request (get-request-line request)))

(defn- attach-headers [request]
  (assoc request :headers (parser/headers request)))

(defn- attach-body [request]
  (assoc request :body (reader/read-body request)))

(defn create [conn]
   (-> {:conn conn}
       attach-request-line
       attach-headers
       attach-body))
