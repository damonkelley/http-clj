(ns http-clj.file
  (:import java.io.File
           java.nio.file.Files
           java.nio.file.Paths))

(defn binary-slurp [path]
  (-> path
      File.
      .toPath
      Files/readAllBytes))

(defn resolve [root-dir path]
  (-> (Paths/get root-dir (into-array [path]))
      .toFile))
