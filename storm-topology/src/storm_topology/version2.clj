(ns storm-topology.version2
    (:require [storm-topology.helpers :as helpers]
              [storm-topology.version1 :as version1]
              [backtype.storm.clojure  :as storm]
              [backtype.storm.config :as storm-config])
    (:import [backtype.storm.spout KestrelThriftSpout]
             [backtype.storm.scheme StringScheme])
    (:gen-class))

(storm/defbolt score-message ["str" "score"] [tuple collector]
               (let [message (.getStringByField tuple "str")]
                    (if (nil? (re-seq #"SPAM" message))
                      (storm/emit-bolt! collector [message 0] :anchor tuple)
                      (storm/emit-bolt! collector [message 1] :anchor tuple))
                    (storm/ack! collector tuple)))

(defn build-topology [queuehost queuename cachekey cachehost cachelimit posthost]
      (storm/topology {"messages" (storm/spout-spec (KestrelThriftSpout. queuehost 2229 queuename (StringScheme.))
                                                    :p 2)}
                      {"scoring" (storm/bolt-spec {"messages" :shuffle}
                                                  score-message
                                                  :p 2)
                       "broadcaster" (storm/bolt-spec {"scoring" :shuffle}
                                                      (version1/broadcaster cachekey cachehost cachelimit posthost)
                                                      :p 2)}))

(defn -main [& name]
      (let [queuehost "ip-10-248-44-154.us-west-2.compute.internal"
            queuename "room1"
            cachekey "room1"
            cachehost "ip-10-248-44-154.us-west-2.compute.internal"
            cachelimit (dec 50)
            posthost "http://ip-10-248-44-154.us-west-2.compute.internal:81"]
           (helpers/bootstrap (build-topology queuehost queuename cachekey cachehost cachelimit posthost)
                              name
                              {storm-config/TOPOLOGY-WORKERS 2})))
