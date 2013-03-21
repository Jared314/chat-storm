(ns status-server.core
    (:require [compojure.core :refer :all]
              [compojure.handler :as handler]
              [compojure.route :as route]
              [ring.adapter.jetty :refer :all]
              [ring.middleware.file-info :refer :all]
              [ring.middleware.resource :refer :all]
              [ring.util.response :as response]
              [clojure.data.json :as json]
              [clojurewerkz.spyglass.client :as memcache])
    (:gen-class))

(def cache-connection (ref nil))
(def cache-key "room1")

(def options-response 
     {:status 200 
      :headers {"Access-Control-Allow-Origin" "*"
                "Access-Control-Allow-Methods" "GET, OPTIONS"}})

(defn get-memcached-status []
      (let [data (memcache/get @cache-connection cache-key)]
               {:data (or data [])}))

(defn get-status [request]
      {:status 200
       :headers {"content-type" "application/json"
                 "Access-Control-Allow-Origin" "*"
                 "Access-Control-Allow-Methods" "GET, OPTIONS"}
       :body (json/write-str (get-memcached-status))})

(defroutes app-routes
           (GET ["/"] {} get-status)
           (OPTIONS ["/"] {} options-response)
           (route/not-found "Page not found"))

(def app (-> app-routes
             handler/site))

(defn -main [& options]
  (let [port (Integer. (nth options 0 5000))
        memcache-ip (nth options 1 "127.0.0.1:11211")]
       (dosync (ref-set cache-connection (memcache/text-connection memcache-ip)))
       (run-jetty app {:port port})))