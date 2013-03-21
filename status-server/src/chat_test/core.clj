(ns chat-test.core
    (:require [lamina.core :refer :all]
              [aleph.http :refer :all]
              [compojure.core :refer :all]
              [ring.middleware.resource :refer :all]
              [ring.middleware.file-info :refer :all]
              [compojure.route :as route]
              [ring.util.response :as response]
              [net.cgrand.enlive-html :refer :all])
    (:gen-class))

(def default-room-name "room1")
(deftemplate page-template "public/index.html" [])

(defn sync-app [request]
      {:status 200
       :headers {"content-type" "text/html"}
       :body (page-template)})

(def wrapped-sync-app
     (-> sync-app
         (wrap-resource "public")
         (wrap-file-info)))

(defn chat-channel-init [ch]
      (receive-all ch (fn [x] true)))

(defn post-message-from-websocket [ch]
      (let [chat-channel (named-channel default-room-name chat-channel-init)]
           (siphon chat-channel ch)
           (siphon ch chat-channel)))

(defn post-message [message]
      (let [chat-channel (named-channel default-room-name chat-channel-init)]
           (enqueue chat-channel message)))

(defn chat [ch request]
      (let [params (:route-params request)]
           (if (:websocket request)
               (post-message-from-websocket ch)
               (enqueue ch (wrapped-sync-app request)))))

(defroutes app-routes
           (GET ["/"] {} (wrap-aleph-handler chat))
           (POST ["/"] {body :body} (post-message (slurp body)) "")
           (route/resources "/static")
           (route/not-found "Page not found"))

(defn -main [& args]
      (let [port (Integer. (str (or (first args) 5000)))]
      (start-http-server (wrap-ring-handler app-routes)
                         {:host "localhost" :port port :websocket true})))