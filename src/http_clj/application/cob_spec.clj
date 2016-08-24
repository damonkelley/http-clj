(ns http-clj.application.cob-spec
  (:require [http-clj.router :refer [route]]
            [http-clj.request-handler :as handler]
            [http-clj.server :refer [run]]
            [http-clj.file :as file-helper]
            [http-clj.application.cob-spec.handlers :refer [fallback]]
            [http-clj.application.cob-spec.cli :as cli]
            [http-clj.application.cob-spec.logging :as logger]))

(defn- cob-spec [request directory]
  (route request {}
         :fallback #(fallback % directory)))

(defn app [directory]
  {:entrypoint #(cob-spec % directory)
   :logger (logger/create)})

(defn -main [& args]
  (let [{options :options} (cli/cli args)]
    (run (app (:directory options)) (:port options))))
