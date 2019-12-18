(ns kemplittle.routes.home
  (:require
   [kemplittle.routes.updates :refer [authenticated? notification-handler]]
   [kemplittle.layout :as layout]
   [kemplittle.db.core :as db]
   [kemplittle.certs.core :as certs]
   [kemplittle.client.session :as session]
   [kemplittle.client.yotiapp :as yotiapp]
   [clojure.java.io :as io]
   [kemplittle.middleware :as middleware]
   [ring.util.response]
   [environ.core :refer [env]]
   [ring.util.http-response :as response]
   [taoensso.timbre :as timbre]
   [ring.middleware.basic-authentication :refer [wrap-basic-authentication]]
   [kemplittle.client.session :refer [get-new-session]])
  )

(defn home-page [request]
  (layout/render request "home.html" {:docs (-> "docs/home.md" io/resource slurp)}))

(defn yotiapp-page [{:keys [params]}]
  (timbre/info "Got request from Yoti servers: " (:token params))
  (yotiapp/pass-token (:token params))
   {:status  200
    :headers {"Content-Type" "text/html; charset=utf-8"}
    :body    "ok"})

(defn about-page [request]
  (let [session (get-new-session)]
    (timbre/info "session:" session)
    (layout/render request "docscan.html" {:session {:id (:session_id session)
                                                   :token (:client_session_token session)}})))

(defn updates-routes []
  ["/updates"
   {:middleware [middleware/wrap-csrf
                 #(-> % (wrap-basic-authentication authenticated?))]}
   ["" {:get notification-handler}]])

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/yotiapp" {:get yotiapp-page}]
   ["/docscan" {:get about-page}]])

