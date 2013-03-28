(ns storm-topology.version2
    (:require [storm-topology.helpers :as helpers]
              [storm-topology.version1 :as version1]
              [backtype.storm.clojure  :as storm]
              [backtype.storm.config :as storm-config]
              [clojure.string :as string])
    (:import [backtype.storm.spout KestrelThriftSpout]
             [backtype.storm.scheme StringScheme])
    (:gen-class))

(storm/defbolt filter-message ["str" "username" "body"] [tuple collector]
               (let [m (.getStringByField tuple "str")
                     i (.indexOf m ":")
                     message (string/triml (.substring m (inc i)))
                     username (.substring m 0 i)]
                    (if (nil? (re-seq #"SPAM" message))
                        (storm/emit-bolt! collector [(str username ": " message) username message] :anchor tuple))
                    (storm/ack! collector tuple)))

(defn build-topology [queuehost queuename cachekey cachehost cachelimit posthost]
      (storm/topology {"messages" (storm/spout-spec (KestrelThriftSpout. queuehost 2229 queuename (StringScheme.))
                                                     :p 1)}
                      {"filtering" (storm/bolt-spec {"messages" :local-or-shuffle}
                                                     filter-message
                                                     :p 2)
                       "broadcaster" (storm/bolt-spec {"filtering" :local-or-shuffle}
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
