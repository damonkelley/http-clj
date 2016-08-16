(ns http-clj.spec-helper.request-generator)

(defn compose-header [header]
  (let [[field-name field-value] header]
    (str field-name ": " field-value "\r\n")))

(defn compose-headers [headers]
  (map compose-header headers))

(defn request [method path headers body]
  (str method " " path " HTTP/1.1\r\n"
       (apply str (compose-headers headers))
       "\r\n"
       body))

(defn GET [path headers]
  (request "GET" path headers ""))

(defn POST [path headers body]
  (request "POST" path headers body))
