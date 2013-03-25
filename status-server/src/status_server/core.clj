(ns status-server.core
    (:require [compojure.core :refer :all]
              [compojure.handler :as handler]
              [compojure.route :as route]
              [ring.adapter.jetty :refer :all]
              [ring.middleware.file-info :refer :all]
              [ring.middleware.resource :refer :all]
              [ring.util.response :as response]
              [clojure.data.json :as json]
              [taoensso.carmine :as car])
    (:gen-class))

(def cache-connection (ref nil))
(def cache-key "room1")
(def pool (car/make-conn-pool))
(def spec-server1 (car/make-conn-spec :host "127.0.0.1" :port 6379))

(def options-response 
     {:status 200 
      :headers {"Access-Control-Allow-Origin" "*"
                "Access-Control-Allow-Methods" "GET, OPTIONS"}})

(defn get-cached-status []
      (let [data (car/with-conn pool spec-server1 (car/lrange cache-key 0 -1))]
                 {:data data}))

(defn get-status [request]
      {:status 200
       :headers {"content-type" "application/json"
                 "Access-Control-Allow-Origin" "*"
                 "Access-Control-Allow-Methods" "GET, OPTIONS"}
       :body (json/write-str (get-cached-status))})

(defroutes app-routes
           (GET ["/"] {} get-status)
           (OPTIONS ["/"] {} options-response)
           (route/not-found "Page not found"))

(def app (-> app-routes
             handler/site))

(defn -main [& options]
  (let [port (Integer. (nth options 0 5000))
        cache-ip (nth options 1 "127.0.0.1:6379")]
       ;(dosync (ref-set cache-connection (memcache/text-connection cache-ip)))
       (run-jetty app {:port port})))