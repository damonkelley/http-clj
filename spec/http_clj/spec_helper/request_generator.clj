(ns http-clj.spec-helper.request-generator)

(defn spread-headers [headers]
  (reduce
    (fn [string [key val]] (str string key ": " val "\r\n"))
    ""
    headers))

(defn request [method path headers body]
  (str method " " path " HTTP/1.1\r\n"
       (spread-headers headers)
       "\r\n"
       body))

(defn GET [path headers]
  (request "GET" path headers ""))

(defn POST [path headers body]
  (request "POST" path headers body))
