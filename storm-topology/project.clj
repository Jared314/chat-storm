(defproject storm-topology "0.0.1-SNAPSHOT"
  :dependencies [[storm/storm-kestrel "0.7.2-SNAPSHOT"]
                 [clojurewerkz/spyglass "1.1.0-beta2"]
                 [clj-http "0.6.5"]
                 [com.taoensso/carmine "1.7.0-beta2"]]
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.4.0"]
                                  [storm "0.8.2"]]}}
  :aot :all
  :min-lein-version "2.0.0")
