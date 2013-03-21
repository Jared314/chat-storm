(ns post-server.core
    (:require [compojure.core :refer :all]
              [compojure.handler :as handler]
              [compojure.route :as route]
              [net.cgrand.enlive-html :refer :all]
              [ring.adapter.jetty :refer :all]
              [ring.middleware.file-info :refer :all]
              [ring.middleware.resource :refer :all]
              [ring.util.response :as response])
    (:gen-class))

;TODO replace lamina with kestrel queue client

(deftemplate page-template "public/index.html" [])

(defn index-page [request]
      {:status 200
       :headers {"content-type" "text/html"}
       :body (page-template)})

(defn post-message [message]
      true)

(defroutes app-routes
           (GET ["/"] {} index-page)
           (POST ["/"] {body :body} (post-message (slurp body)) "")
           (route/resources "/static")
           (route/not-found "Page not found"))

(def app (-> app-routes
             handler/site))

(defn -main [& options]
  (let [port (Integer. (or (first options) 5000))
        mode (keyword (or (second options) :dev))]
       (run-jetty app {:port port})))