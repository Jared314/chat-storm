(defproject post-server "1.0.0-SNAPSHOT"
  :description "Chat server testing"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring "1.1.8"]
                 [enlive "1.1.1"]
                 [clojurewerkz/spyglass "1.1.0-beta3"]]
  :aot [post-server.core]
  :main post-server.core
  :min-lein-version "2.0.0")
