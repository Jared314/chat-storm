(defproject status-server "1.0.0-SNAPSHOT"
  :description "status server testing"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring "1.1.8"]
                 [org.clojure/data.json "0.2.1"]
                 [com.taoensso/carmine "1.7.0-beta2"]]
  :aot [status-server.core]
  :main status-server.core
  :min-lein-version "2.0.0")
