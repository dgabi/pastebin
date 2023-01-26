(ns webapp.core
    (:require [sablono.core :as sab]))

(enable-console-print!)

(println "This text is printed from src/webapp/core.cljs.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text nil :id nil}))



(defn content [data]
  (sab/html [:div
             [:h1 "pastebin"]
             [:input {:type "text" :name "pasteid"}]
             [:input {:type "button"}]
             [:br]
             [:textarea "text"]
             [:br]
             [:p "go"]]))


(defn render! []
  (.render js/ReactDOM
           (content app-state)
           (.getElementById js/document "app")))

(add-watch app-state :on-change (fn [_ _ _ _] (render!)))

(render!)
