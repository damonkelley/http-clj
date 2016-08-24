(ns http-clj.lifecycle
  (:require [http-clj.request :refer [create]]
            [http-clj.response :as response]
            [http-clj.connection :as connection]))

(defn write-response [resp]
  (->> resp
      response/compose
      :message
      (connection/write (:conn resp))))

(defn http [conn {:keys [entrypoint] :as app}]
    (-> conn
        create
        entrypoint
        write-response))
