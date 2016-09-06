(ns http-clj.file
  (:require [clojure.java.io :as io])
  (:import java.io.File
           java.nio.file.Files
           java.nio.file.Paths))

(defn binary-slurp [path]
  (-> path
      File.
      .toPath
      Files/readAllBytes))

(defn binary-slurp-range [path start end]
  (let [buffer-size (inc (- end start))
        buffer (byte-array buffer-size)]
    (with-open [stream (io/input-stream path)]
      (.skip stream start)
      (.read stream buffer))
    buffer))

(defn resolve [root-dir path]
  (-> (Paths/get root-dir (into-array [path]))
      .toFile))
