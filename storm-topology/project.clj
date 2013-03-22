(defproject storm-topology "0.0.1-SNAPSHOT"
  :dependencies []
  :profiles {:dev {:dependencies [[org.clojure/clojure "1.4.0"]
                                  [storm "0.8.2"]]}}
  :main storm-topology.core
  :aot :all
  :min-lein-version "2.0.0")
