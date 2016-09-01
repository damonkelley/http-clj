(ns http-clj.app.cob-spec.handlers
  (:require [http-clj.request-handler :as handler]
            [http-clj.file :as file-helper]
            [http-clj.response :as response]
            [clojure.string :as string]))

(defn static [request directory]
  (let [file (file-helper/resolve directory (:path request))]
    (cond (.isDirectory file) (handler/directory request file)
          (.exists file) (handler/file request file)
          :else (handler/not-found request))))

(defn log [request log]
  (response/create request (.toString log)))

(defn submit-form [request cache]
  (reset! cache (String. (:body request)))
  (response/create request ""))

(defn last-submission [request cache]
  (response/create request @cache))

(defn options [& allowed-options]
  (let [allow (string/join "," allowed-options)]
    #(response/create % "" :headers {"Allow" allow})))
