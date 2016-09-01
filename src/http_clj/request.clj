(ns http-clj.request
  (:require [http-clj.connection :as connection]
            [http-clj.logging :as logging]
            [clojure.string :as string]
            [http-clj.request.parser :as parser]))

(defn- attach-request-line [request]
  (merge request (parser/request-line request)))

(defn- attach-headers [request]
  (assoc request :headers (parser/headers request)))

(defn- attach-body [request]
  (assoc request :body (parser/read-body request)))

(defn create [conn]
   (-> {:conn conn}
       attach-request-line
       attach-headers
       attach-body))
