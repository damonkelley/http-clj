(ns http-clj.spec.integration)

(defn start-server [app port]
  (doto (Thread. #(http-clj.server/run app port))
    (.start)))

(defn shutdown-server
  ([thread]
   (.interrupt thread))
  ([thread f]
   (.interrupt thread)
   (f)))

(defn warmup []
  (Thread/sleep 100))
