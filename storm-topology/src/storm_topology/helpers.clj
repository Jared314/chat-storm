(ns storm-topology.helpers
    (:require [backtype.storm.clojure :refer [local-cluster submit-remote-topology]]
              [backtype.storm.config :refer [TOPOLOGY-DEBUG]])
    (:gen-class))

(defn- run-local! [topology user-options]
      (let [cluster (local-cluster)
            name "debug"
            options (merge {TOPOLOGY-DEBUG true} user-options)]
           (.submitTopology cluster name options topology)
           (Thread/sleep 10000)
           (.shutdown cluster)))

(defn- submit-topology! [name topology user-options]
       (let [options (merge {TOPOLOGY-DEBUG false} user-options)]
            (submit-remote-topology name options topology)))

(defn bootstrap [topology name options]
      (if (nil? name)
          (run-local! topology options)
          (submit-topology! name topology options)))