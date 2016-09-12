(ns http-clj.response.cookies
  (:require [clojure.string :as string]))

(defn format-cookie [name value]
  (str name "=" value))

(defn update-cookies [cookies name value]
  (conj cookies (format-cookie name value)))

(defn set-cookie [response name value]
  (update-in response [:headers :set-cookie] #(update-cookies % name value)))
