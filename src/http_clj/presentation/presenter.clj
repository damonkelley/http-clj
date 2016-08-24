(ns http-clj.presentation.presenter
  (:require [clojure.java.io :as io]))

(defn format-href [request file]
  (-> request
      :path
      io/file
      .toPath
      (.resolve (.getName file))
      .toString))

(defn file [request f]
  {:href (format-href request f)
   :name (.getName f)})

(defn files [request files]
  (map (partial file request) files))
