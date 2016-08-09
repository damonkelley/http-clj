(ns http-clj.echo.application
  (:require [http-clj.connection :as connection]
            [http-clj.server :refer [run]]))

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

(defn -main [& args]
  (run echo-loop 5000))
