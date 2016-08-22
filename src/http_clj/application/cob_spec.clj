(ns http-clj.application.cob-spec
  (:require [http-clj.router :refer [route]]
            [http-clj.response :as response]
            [http-clj.request-handler :as handler]
            [http-clj.server :refer [run]]
            [clojure.java.io :as io])
  (:import java.nio.file.Paths))

(defn index [request]
  (response/create request "cob spec"))

(defn static [path]
  (str "resources/static/" path))

(defn file-helper [root-directory child-path]
  (-> (Paths/get root-directory (into-array [child-path]))
      .toFile))

(defn fallback [request]
  (let [file (file-helper "resources/static/" (:path request))]
    (cond (.isDirectory file) (handler/directory request file)
          (.exists file) (handler/file request file)
          :else (handler/not-found request))))

(defn app [request]
  (route
    request
    {}
    :fallback fallback))

(defn -main [& args]
  (run app 5000))
