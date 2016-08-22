(ns http-clj.request-handler
  (:require [http-clj.file :as f]
            [http-clj.response :as response]
            [clojure.java.io :as io]
            [hiccup.core :refer [html]]))

(defn- directory-template [contents]
  (html
    [:ul (for [file contents]
       [:li [:a {:href file} file]])]))

(defn directory [request dir]
  (response/create
    request
    (directory-template (.list dir))
    :headers {"Content-Type" "text/html"}))

(defn not-found [request]
  (response/create request "Not Found" :status 404))

(defn file
  ([request io-file] ((file (.getPath io-file)) request))
  ([path]
   (fn [request]
     (response/create request (f/binary-slurp path)))))
