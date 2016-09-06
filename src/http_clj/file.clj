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

(defmulti binary-slurp-range
  (fn [_ start end]
    [(type start) (type end)]))

(defmethod binary-slurp-range [Number nil]
  [path start _]
  (let [size (dec (.length (File. path)))]
    (binary-slurp-range path start size)))

(defmethod binary-slurp-range [nil Number]
  [path _ end]
  (let [size (dec (.length (File. path)))
        start (inc (- size end))]
    (binary-slurp-range path start size)))

(defmethod binary-slurp-range [Number Number]
  [path start end]
  (let [buffer-size (inc (- end start))
        buffer (byte-array buffer-size)]
    (with-open [stream (io/input-stream path)]
      (.skip stream start)
      (.read stream buffer))
    buffer))

(defn resolve [root-dir path]
  (-> (Paths/get root-dir (into-array [path]))
      .toFile))
