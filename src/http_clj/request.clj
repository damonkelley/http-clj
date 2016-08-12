(ns http-clj.request
  (:require [http-clj.connection :as connection]))


(defn read-all [conn]
  (loop [conn conn lines []]
    (let [line (connection/readline conn)]
      (if (empty? line)
        lines
        (recur conn (conj lines line))))))

(defn create [conn]
  {:input (read-all conn)
   :conn conn})
