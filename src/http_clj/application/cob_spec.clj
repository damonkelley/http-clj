(ns http-clj.application.cob-spec
  (:require [http-clj.router :refer [route]]
            [http-clj.request-handler :as handler]
            [http-clj.server :refer [run]])
  (:import java.nio.file.Paths))

(defn file-helper [root-directory child-path]
  (-> (Paths/get root-directory (into-array [child-path]))
      .toFile))

(defn fallback [request directory]
  (let [file (file-helper directory (:path request))]
    (cond (.isDirectory file) (handler/directory request file)
          (.exists file) (handler/file request file)
          :else (handler/not-found request))))

(defn- cob-spec [request directory]
  (route request {}
         :fallback #(fallback % directory)))

(defn app [directory]
  #(cob-spec % directory))

(defn -main [& args]
  (run (app "resources/static/") 5000))
