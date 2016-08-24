(ns http-clj.application.cob-spec.handlers
  (:require [http-clj.request-handler :as handler]
            [http-clj.file :as file-helper]
            [http-clj.response :as response]))

(defn fallback [request directory]
  (let [file (file-helper/resolve directory (:path request))]
    (cond (.isDirectory file) (handler/directory request file)
          (.exists file) (handler/file request file)
          :else (handler/not-found request))))

(defn log [request log]
  (response/create request (.toString log)))
