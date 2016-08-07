(ns http-clj.server-spec
  (:require [speclj.core :refer :all]
            [http-clj.server :refer :all]
            [com.stuartsierra.component :as component]))

(def ^:private is-open
 (atom true))

(defn socket []
  (proxy [java.net.Socket] []))

(defn mock-server []
  (proxy [java.net.ServerSocket] []
    (accept []
      (socket))
    (close []
      (reset! is-open false))))

(describe "a server component"
  (with server (create (mock-server)))
  (it "will close the server"
    (should= true @is-open)
    (component/stop @server)
    (should= false @is-open))
  (it "will accept connections"
    (should-be-a java.net.Socket (accept @server))))
