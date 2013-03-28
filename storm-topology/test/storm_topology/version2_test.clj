(ns storm-topology.version2_test
    (:require [clojure.test :refer :all]
              [storm-topology.version2 :as version2]
              [storm.test.util :refer :all]
              [storm.test.visualization :refer :all]
              [backtype.storm.clojure :refer :all]
              [backtype.storm.testing :refer :all]))

(deftest valid-emitted-tuples-shows-up
         (with-quiet-logs
           (with-simulated-time-local-cluster [cluster :supervisors 2]
             (let [queuehost "ec2-54-244-246-137.us-west-2.compute.amazonaws.com"
                   queuename "room1"
                   cachekey "room1"
                   cachehost "ec2-54-244-246-137.us-west-2.compute.amazonaws.com"
                   cachelimit (dec 50)
                   posthost "http://ec2-54-244-246-137.us-west-2.compute.amazonaws.com:81"
                   topology (version2/build-topology queuehost queuename cachekey cachehost cachelimit posthost)
                   results (complete-topology cluster
                                              topology
                                              :mock-sources {"messages" [["testuser1: message1"]
                                                                         ["testuser2: message2"]
                                                                         ["testuser3: SPAM"]
                                                                         ["testuser4: message3"]]})]
                  (is (ms= [["testuser1: message1" "testuser1" "message1"]
                            ["testuser2: message2" "testuser2" "message2"]
                            ["testuser4: message3" "testuser4" "message3"]]
                           (read-tuples results "filtering")))
                  (is (ms= []
                           (read-tuples results "broadcaster")))))))
