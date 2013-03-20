(defproject chat-test "0.0.1"
  :description "Aleph testing"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [aleph "0.3.0-beta11"]
                 [compojure "1.1.5"]
                 [ring "1.1.8"]
                 [enlive "1.1.1"]]
  :aot [chat-test.core]
  :main chat-test.core
  :min-lein-version "2.0.0")
