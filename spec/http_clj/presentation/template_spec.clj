(ns http-clj.presentation.template-spec
  (:require [speclj.core :refer :all]
            [http-clj.presentation.template :as template]
            [http-clj.presentation.presenter :as presenter]
            [hiccup.core :refer [html]]
            [clojure.java.io :as io]))

(describe "template"
  (context "directory"
    (with file-a (io/file "file-a"))
    (with file-b (io/file "file-b"))
    (it "is a list of links to files"
      (let [files (presenter/files {:path "/"} [@file-a @file-b])
            rendered-html (template/directory files)]
        (should= (html [:ul
                        [:li [:a {:href "/file-a"} "file-a"]]
                        [:li [:a {:href "/file-b"} "file-b"]]])
                 rendered-html)))
    (it "lists only the files that are passed to it"
      (should= (html [:ul [:li [:a {:href "/file-a"} "file-a"]]])
               (template/directory [{:href "/file-a" :name "file-a"}])))))
