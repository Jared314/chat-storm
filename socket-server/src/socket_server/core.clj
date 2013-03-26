(ns socket-server.core
    (:require [lamina.core :refer :all]
              [aleph.http :refer :all]
              [compojure.core :refer :all]
              [ring.middleware.resource :refer :all]
              [ring.middleware.file-info :refer :all]
              [compojure.route :as route]
              [ring.util.response :as response])
    (:gen-class))

(def default-room-name "room1")
(def options-response 
     {:status 200 
      :headers {"Access-Control-Allow-Origin" "*"
                "Access-Control-Allow-Methods" "GET, POST, OPTIONS"}})

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
               nil)))

(defroutes app-routes
           (GET ["/"] {} (wrap-aleph-handler chat))
           (POST ["/"] {body :body} (if (not (nil? body)) (post-message (slurp body))) {:status 200 :headers {"Access-Control-Allow-Origin" "*" "Access-Control-Allow-Methods" "GET, POST, OPTIONS"} :body ""})
           (OPTIONS ["/"] {} options-response)
           (route/not-found "Page not found"))

(defn -main [& args]
      (let [port (Integer. (str (or (first args) 5000)))]
      (start-http-server (wrap-ring-handler app-routes)
                         {:host "localhost" :port port :websocket true})))