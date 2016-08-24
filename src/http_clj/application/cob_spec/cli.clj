(ns http-clj.application.cob-spec.cli
  (:require [clojure.java.io :as io]
            [clojure.tools.cli :refer [parse-opts]]))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :default 5000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]

   ["-d" "--directory DIR" "Directory to serve"
    :default "./public"
    :validate [#(.isDirectory (io/file %)) "Must be a valid directory"]]])

(defn cli [args]
  (parse-opts args cli-options))
