(ns http-clj.request.parser.request-line-spec
  (:require [speclj.core :refer :all]
            [http-clj.request.parser.request-line :as rql]))

(describe "request.parser.request-line"
  (context "parse"
    (it "parses the request line into a map"
      (should= {:method "GET" :path "/" :version "HTTP/1.1" :query-params nil}
               (rql/parse "GET / HTTP/1.1"))
      (should= {:method "POST" :path "/file" :version "HTTP/1.0" :query-params nil}
               (rql/parse "POST /file HTTP/1.0")))

    (it "parses the query parameters"
      (let [request-line (rql/parse "GET /file?key=value HTTP/1.1")]
        (should= {"key" "value"} (:query-params request-line))))

    (it "strips the query string from the path"
      (let [request-line (rql/parse "GET /file?key=value HTTP/1.1")]
        (should= "/file" (:path request-line))))

    (it "always has :path, :version :method, and :query-params"
      (should= {:method "" :path "" :version "" :query-params nil}
               (rql/parse ""))))

  (context "parse-path"
    (it "strips the query string from the path"
      (let [request-line {:path "/file?key=value"}]
        (should= "/file" (:path (rql/parse-path request-line)))))

    (it "adds the query parameters to the map"
      (let [request-line {:path "/file?key=value&parameter=other-value"}]
        (should= {"key" "value" "parameter" "other-value"}
                 (:query-params (rql/parse-path request-line)))))

    (it "it decodes parameter fields"
      (let [request-line {:path "/file?operators=%26%2C%3D%3B&sort=true"}]
        (should= {"operators" "&,=;" "sort" "true"}
                 (:query-params (rql/parse-path request-line)))))

    (it ":query-params is nil if there is not query string"
      (let [request-line {:method "GET" :path "/file" :version "HTTP/1.1"}]
        (should= nil
                 (:query-params (rql/parse-path request-line)))))))

