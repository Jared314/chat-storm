(ns storm-topology.helpers
    (:require [backtype.storm.clojure :as storm]
              [backtype.storm.config :refer [TOPOLOGY-DEBUG]])
    (:import [backtype.storm StormSubmitter]
             [backtype.storm.generated SubmitOptions TopologyInitialStatus])
    (:gen-class))

(defn- run-local! [topology user-options]
      (let [cluster (storm/local-cluster)
            name "debug"
            options (merge {TOPOLOGY-DEBUG true} user-options)]
           (.submitTopology cluster name options topology)
           (Thread/sleep 10000)
           (.shutdown cluster)))

(defn- submit-topology! [name topology user-options]
       (let [options (merge {TOPOLOGY-DEBUG false} user-options)]
            (StormSubmitter/submitTopology name options topology (SubmitOptions. TopologyInitialStatus/INACTIVE))))

(defn bootstrap [topology name options]
      (if (nil? name)
          (run-local! topology options)
          (submit-topology! name topology options)))