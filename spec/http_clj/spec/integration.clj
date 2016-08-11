(ns http-clj.spec.integration
  (:import java.util.concurrent.CountDownLatch))

(defn new-latch []
  (CountDownLatch. 1))

(defn start-server [app port latch]
  (doto (Thread. #(http-clj.server/run app port latch))
    (.start)))

(defn shutdown-server
  ([thread]
   (.interrupt thread))
  ([thread f]
   (.interrupt thread)
   (f)))
