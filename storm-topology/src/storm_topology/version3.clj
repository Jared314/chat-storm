(ns storm-topology.version3
    (:require [storm-topology.helpers :as helpers]
              [storm-topology.version1 :as version1]
              [storm-topology.version2 :as version2]
              [backtype.storm.clojure  :as storm]
              [backtype.storm.config :as storm-config])
    (:import [backtype.storm.spout KestrelThriftSpout]
             [backtype.storm.scheme StringScheme])
    (:gen-class))

(defn- tick? [tuple] 
       (= "__tick" (.getSourceStreamId tuple)))

(defn- emit [collector messages]
       (dorun (map #(storm/emit-bolt! collector [(str % ": " (messages %))])
                   (keys messages))))

(storm/defbolt agg-messages 
               ["str"]
               {:prepare true storm-config/TOPOLOGY_TICK_TUPLE_FREQ_SECS 5}
               [conf context collector]
               (let [messages (ref {})]
                    (storm/bolt-execute [tuple]
                                        (if (tick? tuple)
                                            (emit collector @messages)
                                            (let [message (.getStringByField tuple "body")
                                                  username (.getStringByField tuple "username")]
                                                 (dosync (assoc @messages
                                                                username
                                                                (str (@messages username) 
                                                                     (if (contains? @messages username) "\n") 
                                                                     message))
                                                         (storm/ack! collector tuple)))))))

(defn build-topology [queuehost queuename cachekey cachehost cachelimit posthost]
      (storm/topology {"messages" (storm/spout-spec (KestrelThriftSpout. queuehost 2229 queuename (StringScheme.))
                                                    :p 1)}
                      {"filtering" (storm/bolt-spec {"messages" :local-or-shuffle}
                                                  version2/filter-message
                                                  :p 2)
                       "rate-limited-messages" (storm/bolt-spec {"filtering" ["username"]}
                                                                agg-messages
                                                                :p 2)
                       "broadcaster" (storm/bolt-spec {"rate-limited-messages" :local-or-shuffle}
                                                      (version1/broadcaster cachekey cachehost cachelimit posthost)
                                                      :p 2)}))

(defn -main [& name]
      (let [queuehost "ec2-54-244-246-137.us-west-2.compute.amazonaws.com"
            queuename "room1"
            cachekey "room1"
            cachehost "ec2-54-244-246-137.us-west-2.compute.amazonaws.com"
            cachelimit (dec 50)
            posthost "http://ec2-54-244-246-137.us-west-2.compute.amazonaws.com:81"]
           (helpers/bootstrap (build-topology queuehost queuename cachekey cachehost cachelimit posthost)
                              (first name)
                              {storm-config/TOPOLOGY-WORKERS 2})))