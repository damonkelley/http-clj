(ns http-clj.file
  (:require [clojure.java.io :as io])
  (:import java.io.File
           java.nio.file.Files
           java.nio.file.Paths
           java.net.URLConnection))

(defn binary-slurp [path]
  (-> path
      File.
      .toPath
      Files/readAllBytes))

(defn- -binary-slurp-range [path start end]
  (let [buffer-size (inc (- end start))
        buffer (byte-array buffer-size)]
    (with-open [stream (io/input-stream path)]
      (.skip stream start)
      (.read stream buffer))
    buffer))

(defn- valid-range? [path start end]
  (let [size (-> (File. path) .length dec)]
    (and (<= start size) (<= end size))))

(defn binary-slurp-range [path start end]
  (when-not (valid-range? path start end)
    (throw (ex-info "Range Unsatisfiable" {:cause :unsatisfiable})))
  (-binary-slurp-range path start end))

(defmulti query-range
  (fn [_ start end]
    [(type start) (type end)]))

(defmethod query-range [Number Number]
  [path start end]
  {:range (binary-slurp-range path start end)
   :length (.length (io/file path))
   :start start
   :end end})

(defmethod query-range [nil Number]
  [path _ end]
  (let [last-byte-position (dec (.length (io/file path)))
        start (inc (- last-byte-position end))]
    (query-range path start last-byte-position)))

(defmethod query-range [Number nil]
  [path start _]
  (let [last-byte-position (dec (.length (io/file path)))]
    (query-range path start last-byte-position)))

(defn content-type-of [path]
  (URLConnection/guessContentTypeFromName path))

(defn resolve [root-dir path]
  (-> (Paths/get root-dir (into-array [path]))
      .toFile))
