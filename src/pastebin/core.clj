(ns pastebin.core
  (:require [integrant.core :as ig]
            [muuntaja.core :as m]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [reitit.coercion.schema :as rcs]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as rrmm]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as rr]
            [clojure.core.async :as a :refer [>! <! >!! <!! go chan put!
                                              buffer close! thread alts! alts!!
                                              timeout]]
            [ring.logger :as logger])
  (:import java.util.Base64))

(defn encode-base64 [to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(defn decode-base64 [to-decode]
  (String. (.decode (Base64/getDecoder) to-decode)))

(defn md5sum [s]
  (let [md5 (java.security.MessageDigest/getInstance "MD5")
        raw (.digest md5 (.getBytes s))]
    (format "%032x" (BigInteger. 1 raw))))

(def save-chan (chan 5))

(defn save-to-file [v]
  (spit "test/pastebin/filestore.data" v))

(go (while true (save-to-file (<! save-chan))))

(defn insert [db k v]
  (do
    (swap! db assoc k v)
    (put! save-chan (str k " " (encode-base64 v)))
    {:key k}))

(defn add-item
  ([db k v]
   (insert db k v))
  ([db v]
   (insert db (md5sum v) v)))

(defn get-item [db id]
  (let [r (@db id)]
    {:paste r}))

(defn init-from-file [file-path store]
  (with-open [rdr (io/reader file-path)]
    (dorun (map #(let [line-split (str/split % #" ")]
                   (add-item store
                             (first line-split)
                             (decode-base64
                              (last line-split))))
                (line-seq rdr)))))

(defn ping [_]
  {:status 200 :body "ok"})

(defn add-paste [req]
  (let [data (get-in req [:params :data]) db (req :db)]
    (if (not (nil? data))
      (if (> 1025 (alength (.getBytes data "UTF-8")))
        (-> (add-item db data)
            rr/response)
        (rr/bad-request {:message "payload larger than 1024 bytes"}))
      (rr/bad-request {:message "missing form parameter 'data'"}))))

(defn get-paste [request]
  (let [id (get-in request [:path-params :id])
        db (:db request)
        rslt (get-item db id)]
    (if (nil? (get rslt :paste))
      (rr/not-found rslt)
      (rr/response rslt))
    ))

(def middleware-insert-db
  {:name ::db
   :compile (fn [{:keys [db]} _]
              (fn [handler]
                (fn [req]
                  (handler (assoc req :db db)))))})

(defn app [db]
  (ring/ring-handler
   (ring/router
    [["/ping" {:get ping}]
     ["/add" {:post add-paste}]
     ["/get/:id"
      {:parameters {:path {:id schema.core/Str}} :get get-paste}]]

    {:data {:muuntaja   m/instance
            :coercion   rcs/coercion
            :middleware [middleware-insert-db
                         rrmm/format-middleware
                         rrc/coerce-exceptions-middleware
                         rrc/coerce-response-middleware
                         rrc/coerce-request-middleware
                         parameters/parameters-middleware
                         wrap-keyword-params
                         [wrap-cors :access-control-allow-origin  #".*"
                          :access-control-allow-methods [:get :put :post :patch :delete]]]
            :db db}})
   (ring/routes
    (ring/create-default-handler
     {:not-found (constantly {:status 404 :body "not found"})
      :method-not-allowed (constantly {:status 405 :body "not allowed"})}))))

(defn init-db [file]
  (let [db (atom {})]
    (do
      (init-from-file file db)
      {:db db})))

(def config
  {:adapter/jetty {:handler (ig/ref :handler/run-app) :port 4123}
   :handler/run-app (init-db "test/pastebin/filestore.data")})

(defmethod ig/init-key :adapter/jetty [_ {:keys [handler] :as opts}]
  (run-jetty (-> handler
                 logger/wrap-log-response)
             (-> opts
                 (dissoc handler)
                 (assoc :join? false))))

(defmethod ig/init-key :handler/run-app [_ {:keys [db]}]
  (app db))

(defn -main []
  (ig/init  config))
