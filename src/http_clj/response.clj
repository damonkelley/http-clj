(ns http-clj.response)

(defn create [body]
  {:body body})

(defn compose [resp]
  (str "HTTP/1.1 200 OK" \return \newline
       \return \newline
       (:body resp)
       \return \newline))
