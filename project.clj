(defproject http-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/data.codec "0.1.0"]
                 [com.stuartsierra/component  "0.3.1"]
                 [clj-http "2.2.0"]
                 [com.taoensso/timbre "4.7.4"]
                 [hiccup "1.0.5"]]
  :profiles {:dev {:dependencies [[speclj "3.3.1"]]}}
  :plugins [[speclj "3.3.1"]]
  :test-paths ["spec"])
