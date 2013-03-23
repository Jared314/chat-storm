; References: 
;   https://github.com/nathanmarz/storm-deploy/wiki
;   http://palletops.com/doc/faq/jclouds-aws/
(defpallet
  :services {
    :default {
      :blobstore-provider "aws-s3"
      :provider "aws-ec2"
      :environment {:user {:username "storm"
                           :private-key-path "/Users/User1/Desktop/storm_cluster.pem"
                           ; the public key must match the <private key fullpath>.pub pattern
                           :public-key-path "/Users/User1/Desktop/storm_cluster.pem.pub"}
                    :aws-user-id "9999-9999-9999"}
      :identity "XXXXXXXXXXXXXXXXXXXX"
      :credential "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
      
      ; Region must match the region used in the storm-deploy cluster.yaml file
      ; Region must not include the availability zone
      :jclouds.regions "us-east-1" 

      ; Limit the jclouds ami search to a single owner for faster startup
      ;:jclouds.ec2.ami-query "owner-id=999999999999"
    }})