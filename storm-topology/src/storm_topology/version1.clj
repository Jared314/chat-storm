(ns storm-topology.version1
    (:require [storm-topology.helpers :as helpers]
              [backtype.storm.clojure  :as storm]
              [backtype.storm.config :as storm-config]
              [clj-http.client :as http]
              [taoensso.carmine :as redis])
    (:import [backtype.storm.spout KestrelThriftSpout]
             [backtype.storm.scheme StringScheme])
    (:gen-class))

(storm/defbolt broadcaster [] 
               {:prepare true :params [cachekey cachehost cachelimit posthost]} 
               [conf context collector]
               (let [pool (redis/make-conn-pool)
                     spec-server1 (redis/make-conn-spec :host cachehost :port 6379)]
                    (storm/bolt-execute [tuple]
                      (let [message (.getStringByField tuple "str")]
                           (http/post posthost {:body message})
                           (redis/with-conn pool spec-server1
                                            (redis/multi)
                                            (redis/lpush cachekey message)
                                            (redis/ltrim cachekey 0 cachelimit)
                                            (redis/exec))
                           (storm/ack! collector tuple)))))

(defn build-topology [queuehost queuename cachekey cachehost cachelimit posthost]
      (storm/topology {"messages" (storm/spout-spec (KestrelThriftSpout. queuehost 2229 queuename (StringScheme.))
                                                    :p 1)}
                      {"broadcaster" (storm/bolt-spec {"messages" :local-or-shuffle}
                                                      (broadcaster cachekey cachehost cachelimit posthost)
                                                      :p 1)}))

(defn -main [& name]
      (let [queuehost "ec2-54-244-246-137.us-west-2.compute.amazonaws.com"
            queuename "room1"
            cachekey "room1"
            cachehost "ec2-54-244-246-137.us-west-2.compute.amazonaws.com"
            cachelimit (dec 50)
            posthost "http://ec2-54-244-246-137.us-west-2.compute.amazonaws.com:81"]
           (helpers/bootstrap (build-topology queuehost queuename cachekey cachehost cachelimit posthost)
                              (first name)
                              {})))
