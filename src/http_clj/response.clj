(ns http-clj.response)

(defn create [request body]
  {:body body
   :conn (:conn request)})

(defn compose [resp]
  (assoc resp :message (str "HTTP/1.1 200 OK\r\n"
                            "\r\n"
                            (:body resp)
                            "\r\n")))
