(ns http-clj.request.validator)

(defn validate [{:keys [method path] :as request}]
  (if (some empty? [method path])
    (assoc request :valid? false)
    (assoc request :valid? true)))
