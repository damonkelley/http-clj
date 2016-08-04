(ns http-clj.core
 (:require [http-clj.connection :as connection])
 (:import java.net.ServerSocket))


(defn- echo [text conn]
  (connection/write conn text)
  conn)

(defn echo-loop [conn]
  (loop [conn conn]
   (let [text (connection/readline conn)]
     (if (= "bye." text)
       (echo "Goodbye" conn)
       (recur (echo text conn))))))
