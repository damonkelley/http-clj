(ns http-clj.response
  (:require [clojure.string :as string]
            [http-clj.response.formatter :as formatter]
            [http-clj.connection :as connection]))

(defn create [request body & {:keys [status headers]
                              :or {status 200
                                   headers {}}}]
  {:body body
   :headers headers
   :status status
   :conn (:conn request)})

(defn write [resp]
  (->> resp
      formatter/format-message
      :message
      (connection/write (:conn resp))))
