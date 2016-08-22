(ns http-clj.file
  (:import java.io.File
           java.nio.file.Files))

(defn binary-slurp [path]
  (-> path
      File.
      .toPath
      Files/readAllBytes))
