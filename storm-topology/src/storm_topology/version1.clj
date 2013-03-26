(ns storm-topology.version1
    (:require [storm-topology.helpers :as helpers]
              [backtype.storm.clojure  :as storm]
              [backtype.storm.config :as storm-config]
              [clj-http.client :as http]
              [taoensso.carmine :as redis])
    (:import [backtype.storm.spout KestrelThriftSpout]
             [backtype.storm.scheme StringScheme])
    (:gen-class))

(def queuename "room1")
(def queuehost "ip-10-248-44-154.us-west-2.compute.internal")
(def cachekey "room1")
(def cachehost "ip-10-248-44-154.us-west-2.compute.internal")
(def cachelimit (dec 50))
(def posthost "http://ip-10-248-44-154.us-west-2.compute.internal:81")

(def pool         (redis/make-conn-pool))
(def spec-server1 (redis/make-conn-spec :host cachehost :port 6379))

(storm/defbolt broadcaster [] [tuple collector]
         (let [message (.getStringByField tuple "str")]
              (http/post posthost {:body message})
              (redis/with-conn pool spec-server1
                             (redis/multi)
                             (redis/lpush cachekey message)
                             (redis/ltrim cachekey 0 cachelimit)
                             (redis/exec))
              (storm/ack! collector tuple)))

(defn build-topology []
      (storm/topology {"message source" (storm/spout-spec (KestrelThriftSpout. queuehost 2229 queuename (StringScheme.))
                                                          :p 2)}
                      {"broadcaster" (storm/bolt-spec {"message source" :shuffle}
                                                      broadcaster
                                                      :p 2)}))

(defn -main [& name] 
      (helpers/bootstrap (build-topology) name {storm-config/TOPOLOGY-WORKERS 2}))
