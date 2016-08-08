(ns http-clj.echo
  (:require [http-clj.connection :as connection]))

(def exit-signal "bye.")
(def exit-message "Goodbye")

(defn- echo [text conn]
  (connection/write conn (str text \newline))
  conn)

(defn echo-loop [conn]
  (let [text (connection/readline conn)]
    (if (= exit-signal text)
      (echo exit-message conn)
      (recur (echo text conn)))))
