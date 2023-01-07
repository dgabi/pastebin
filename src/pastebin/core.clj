(ns pastebin.core
  (:require [integrant.core :as ig]
            [muuntaja.core :as m]
            [pastebin.filestore :as filestore]
            [pastebin.store :as store]
            [reitit.coercion.schema :as rcs]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as rrc]
            [reitit.ring.middleware.muuntaja :as rrmm]
            [reitit.ring.middleware.parameters :as parameters]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.util.response :as rr]))

(defn ping [_]
  {:status 200 :body "ok"})

(defn add-paste [req]
  (let [data (get-in req [:params :data]) db (req :db)]
    (if (not (nil? data))
      (-> (store/add-item db data)
          rr/response)
      (rr/bad-request {"messsage" "missing form parameter 'data'"})
    )))

(defn get-paste [request]
  (let [id (get-in request [:path-params :id]) db (:db request)]
      (-> (store/get-item db id)
          rr/response)))

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
     {
      :not-found (constantly {:status 404 :body "not found"})
      :method-not-allowed (constantly {:status 405 :body "not allowed"})}))))

(defn init-db [file]
  (let [db (atom {})]
    (do
      (filestore/init-from-file file db)
      {:db db})
    ))

(def config
  {:adapter/jetty {:handler (ig/ref :handler/run-app) :port 4123}
   :handler/run-app (init-db "test/pastebin/filestore.data")})

(defmethod ig/init-key :adapter/jetty [_ {:keys [handler] :as opts}]
  (run-jetty handler (-> opts (dissoc handler) (assoc :join? false))))

(defmethod ig/init-key :handler/run-app [_ {:keys [db]}]
  (app db))

(defn -main []
  (ig/init  config))
