(ns http-clj.application.cob-spec
  (:require [http-clj.router :refer [route]]
            [http-clj.request-handler :as handler]
            [http-clj.server :refer [run]]
            [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]])
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

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 5000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]

   ["-d" "--directory DIR" "Directory to serve"
    :default "./public"
    :validate [#(.isDirectory (io/file %)) "Must be a valid directory"]]])

(defn cli [args]
  (-> args
      (parse-opts cli-options)))

(defn -main [& args]
  (let [{options :options} (cli args)]
    (run (app (:directory options)) (:port options))))
