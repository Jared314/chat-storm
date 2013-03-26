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
                                                    :p 2)}
                      {"broadcaster" (storm/bolt-spec {"messages" :shuffle}
                                                      (broadcaster cachekey cachehost cachelimit posthost)
                                                      :p 2)}))

(defn -main [& name]
      (let [queuehost "ip-10-248-44-154.us-west-2.compute.internal"
            queuename "room1"
            cachekey "room1"
            cachehost "ip-10-248-44-154.us-west-2.compute.internal"
            cachelimit (dec 50)
            posthost "http://ip-10-248-44-154.us-west-2.compute.internal:81"]
           (helpers/bootstrap (build-topology queuehost queuename cachekey cachehost cachelimit posthost)
                              (first name)
                              {storm-config/TOPOLOGY-WORKERS 2})))
