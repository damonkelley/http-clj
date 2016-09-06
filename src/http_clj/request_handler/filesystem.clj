(ns http-clj.request-handler.filesystem
  (:require [http-clj.file :as f]
            [http-clj.response :as response]
            [http-clj.presentation.template :as template]
            [http-clj.presentation.presenter :as presenter]))

(defn directory [request dir]
  (let [files (presenter/files request (.listFiles dir))
        html (template/directory files)]
    (response/create request html :headers {"Content-Type" "text/html"})))

(defn file [{:keys [headers] :as request} io-file]
  (let [path (.getPath io-file)]
    (if-let [{start :start end :end} (:range headers)]
      (response/create request (f/binary-slurp-range path start end) :status 206)
      (response/create request (f/binary-slurp path)))))
