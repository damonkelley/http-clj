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

(defn binary-slurp-range [path start end]
  (let [buffer-size (inc (- end start))
        buffer (byte-array buffer-size)]
    (with-open [stream (io/input-stream path)]
      (.skip stream start)
      (.read stream buffer))
    buffer))

(defn- last-byte-of [path]
  (-> path File. .length dec))

(defn- length-of [path]
  (.length (File. path)))

(defn- valid-range? [path start end]
  (let [last-byte (last-byte-of path)]
    (and (<= start last-byte) (<= end last-byte))))

(defmulti query-range
  (fn [_ start end]
    [(type start) (type end)]))

(defn- -query-range [path start end]
  {:range (binary-slurp-range path start end)
   :length (length-of path)
   :start start
   :end end})

(defmethod query-range [Number Number]
  [path start end]
  (when-not (valid-range? path start end)
    (throw
      (ex-info "Range Unsatisfiable"
               {:cause :unsatisfiable
                :length (length-of path)})))
  (-query-range path start end))

(defmethod query-range [nil Number]
  [path _ end]
  (let [last-byte (last-byte-of path)
        start (inc (- last-byte end))]
    (query-range path start last-byte)))

(defmethod query-range [Number nil]
  [path start _]
  (let [last-byte (last-byte-of path)]
    (query-range path start last-byte)))

(defn content-type-of [path]
  (URLConnection/guessContentTypeFromName path))

(defn resolve [root-dir path]
  (-> (Paths/get root-dir (into-array [path]))
      .toFile))
