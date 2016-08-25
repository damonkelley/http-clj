(ns http-clj.app.cob-spec.logging-spec
  (:require [speclj.core :refer :all]
            [http-clj.app.cob-spec.logging :as logger]
            [http-clj.logging :as logging]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            [clojure.java.io :as io])
  (:import java.io.ByteArrayOutputStream))

(describe "cob-spec logger"
  (with output (ByteArrayOutputStream.))

  (with test-config
    {:level :debug
     :appenders {:println (appenders/println-appender {:stream (io/writer @output)})}})

  (it "creates a logger"
    (should= true (satisfies? logging/Logger (logger/create @test-config))))

  (it "logs the contents"
    (logging/log (logger/create @test-config) :info "message")
    (should-contain "message" (.toString @output)))

  (it "has a default configuration"
    (let [log-file "resources/log/log.txt"]
      (spit log-file "")
      (should-be empty? (slurp log-file))
      (logging/log (logger/create) :debug "message")
      (should-contain "message" (slurp log-file))
      (should-contain "DEBUG" (slurp log-file)))))
