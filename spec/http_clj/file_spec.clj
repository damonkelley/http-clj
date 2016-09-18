(ns http-clj.file-spec
  (:require [speclj.core :refer :all]
            [http-clj.file :as file]
            [clojure.java.io :as io])
  (:import java.io.File))

(describe "file"
  (with temp-file (File/createTempFile "http-clj-test-file" ".txt"))
  (with test-path (.getPath @temp-file))
  (after (.delete @temp-file))

  (context "binary-slurp"
    (with contents "the quick brown fox")
    (before (spit @test-path @contents))

    (it "reads the file contents into a byte-array"
      (should= (seq (.getBytes @contents))
               (seq (file/binary-slurp @test-path)))))

  (describe "query-file-range"
    (before (spit @test-path "Query a range of bytes"))

    (it "can read the first 4 bytes of a file"
      (let [range-info (file/query-range @test-path 0 4)]
        (should= "Query" (String. (:range range-info)))))

    (it "can read bytes 1 through 4 of the file"
      (let [range-info (file/query-range @test-path 1 5)]
        (should= "uery " (String. (:range range-info)))))

    (it "raises an exception if offset is outside of the length of the file"
      (should-throw clojure.lang.ExceptionInfo
                    (file/query-range @test-path 0 5000))
      (should= {:cause :unsatisfiable :length 22}
               (try (file/query-range @test-path 0 5000)
                    (catch Exception e
                      (ex-data e)))))

    (it "determines the length of the file"
      (spit @test-path "a")
      (should= 1 (:length (file/query-range @test-path 0 0)))

      (spit @test-path "abcdefg")
      (should= 7 (:length (file/query-range @test-path 0 0))))

    (it "has the start position of the range"
      (should= 0 (:start (file/query-range @test-path 0 3)))
      (should= 4 (:start (file/query-range @test-path 4 6))))

    (it "has the end position of the range"
      (should= 3 (:end (file/query-range @test-path 0 3)))
      (should= 6 (:end (file/query-range @test-path 4 6))))

    (it "accepts a nil start position"
      (let [range-info (file/query-range @test-path nil 3)]
        (should= {:start 19 :end 21 :length 22} (dissoc range-info :range))
        (should= "tes" (String. (:range range-info))))

      (let [range-info (file/query-range @test-path nil 5)]
        (should= {:start 17 :end 21 :length 22} (dissoc range-info :range))
        (should= "bytes" (String. (:range range-info)))))

    (it "accepts a nil end position"
      (let [range-info (file/query-range @test-path 6 nil)]
        (should= {:start 6 :end 21 :length 22} (dissoc range-info :range))
        (should= "a range of bytes" (String. (:range range-info))))

      (let [range-info (file/query-range @test-path 17 nil)]
        (should= {:start 17 :end 21 :length 22} (dissoc range-info :range))
        (should= "bytes" (String. (:range range-info))))))

  (context "binary-slurp-range"
    (before (spit @test-path "Read a range of bytes"))

    (it "can read the first 4 bytes of a file"
      (let [byte-range (file/binary-slurp-range @test-path 0 3)]
        (should= "Read" (String. byte-range))))

    (it "can read bytes 1 through 4 of the file"
      (let [byte-range (file/binary-slurp-range @test-path 1 4)]
        (should= "ead " (String. byte-range)))))

  (describe "content-type-of"
    (it "it determines the content type of the file"
      (should= "text/plain" (file/content-type-of "file.txt"))
      (should= "image/gif" (file/content-type-of "image.gif"))))

  (context "resolve"
    (it "takes two paths"
      (file/resolve "resources/static" "image.gif"))

    (it "it forms the path correctly"
      (should= "resources/static/image.gif"
               (.getPath (file/resolve "resources/static/" "/image.gif"))))

    (it "returns a file at the requsted path"
      (should= "resources/static/image.gif"
               (.getPath (file/resolve "resources/static" "image.gif"))))))
