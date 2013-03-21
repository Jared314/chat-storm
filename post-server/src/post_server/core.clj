(ns post-server.core
    (:require [compojure.core :refer :all]
              [compojure.handler :as handler]
              [compojure.route :as route]
              [net.cgrand.enlive-html :refer :all]
              [ring.adapter.jetty :refer :all]
              [ring.middleware.file-info :refer :all]
              [ring.middleware.resource :refer :all]
              [ring.util.response :as response]
              [clojurewerkz.spyglass.client :as kestrel])
    (:gen-class))

(def queue-connection (ref nil))
(def queue-name "room1")
(deftemplate page-template "public/index.html" [])

(defn index-page [request]
      {:status 200
       :headers {"content-type" "text/html"}
       :body (page-template)})

(defn post-message [message]
      (let [data [message]]
           (kestrel/set queue-connection queue-name 0 data)))

(defroutes app-routes
           (GET ["/"] {} index-page)
           (POST ["/"] {body :body} (post-message (slurp body)) "")
           (route/resources "/static")
           (route/not-found "Page not found"))

(def app (-> app-routes
             handler/site))

(defn -main [& options]
  (let [port (Integer. (nth options 0 5000))
        kestrel-ip (nth options 1 "127.0.0.1:22133")]
       (dosync (ref-set queue-connection (kestrel/text-connection kestrel-ip)))
       (run-jetty app {:port port})))