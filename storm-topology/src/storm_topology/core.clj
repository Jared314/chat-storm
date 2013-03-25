(ns storm-topology.core
    (:require [backtype.storm.clojure  :refer :all]
              [backtype.storm.config :refer :all]
              [clojurewerkz.spyglass.client :as mclient]
              [clj-http.client :as http]
              [taoensso.carmine :as car])
    (:import [backtype.storm StormSubmitter LocalCluster]
             [backtype.storm.spout KestrelThriftSpout]
             [backtype.storm.scheme StringScheme])
    (:gen-class))

(def queuename "room1")
(def queuehost "54.244.246.137")
(def cachekey "room1")
(def cachehost "54.244.246.137")
(def cachelimit (dec 50))
(def posthost "http://54.244.246.137:81")

(def pool         (car/make-conn-pool))
(def spec-server1 (car/make-conn-spec :host cachehost :port 6379))

(defbolt broadcaster [] [tuple collector]
         (let [message (.getStringByField tuple "str")]
              (http/post posthost {:body message})
              (car/with-conn pool spec-server1
                             (car/multi)
                             (car/lpush cachekey message)
                             (car/ltrim cachekey 0 cachelimit)
                             (car/exec))
              (ack! collector tuple)))

(defn build-topology []
      (topology {"message source" (spout-spec (KestrelThriftSpout. queuehost 2229 queuename (StringScheme.)))}
                {"broadcaster" (bolt-spec {"message source" :shuffle}
                                broadcaster
                                :p 2)}))

(defn run-local! [topology]
      (let [cluster (LocalCluster.)]
           (.submitTopology cluster "broadcaster" {TOPOLOGY-DEBUG true} topology)
           (Thread/sleep 10000)
           (.shutdown cluster)))

(defn submit-topology! [name topology]
      (let [options {TOPOLOGY-DEBUG false TOPOLOGY-WORKERS 2}]
           (StormSubmitter/submitTopology name options topology)))

(defn -main
      ([] (run-local! (build-topology)))
      ([name] (submit-topology! name (build-topology))))