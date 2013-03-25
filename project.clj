(defproject chat-test "0.1.0-SNAPSHOT"
            :description ""
            :dependencies []
            :min-lein-version "2.0.0"
            :plugins [[lein-sub "0.2.4"]]
            :sub ["post-server"
                  "socket-server"
                  "status-server"
                  "storm-topology"])