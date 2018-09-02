(ns carto-challenge.service
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [io.pedestal.http.body-params :as body-params]
            [ring.util.response :as ring-resp]
            [carto-challenge.activities :as act]
            [carto-challenge.data :as d]
            [java-time :as t]
            [clojure.data.json :as json]))

(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s - served from %s"
                              (clojure-version)
                              (route/url-for ::about-page))))

(defn- normalize-input [input]
  (if (vector? input)
    input
    (conj [] input)))

(defn- normalize-output [key value]
  (if (nil? value) "-" value))

(defn- json-response [activities]
  (ring-resp/response
   (json/write-str activities
                   :value-fn normalize-output)))

(defn activities-page [request]
  (let [exclude-category (get-in request [:query-params :excludeCategory])
        exclude-location (get-in request [:query-params :excludeLocation])
        exclude-district (get-in request [:query-params :excludeDistrict])]
    (json-response
     (act/find-activities (normalize-input exclude-category)
                          (normalize-input exclude-location)
                          (normalize-input exclude-district)
                          act/activities))))

(defn recommendations-page [request]
  (let [start (get-in request [:query-params :start])
        end (get-in request [:query-params :end])
        category (get-in request [:query-params :category])]
    (json-response
     (act/recommendations category
                          start
                          end
                          (t/local-date)
                          act/activities))))

;; Defines "/" and "/about" routes with their associated :get handlers.
;; The interceptors defined after the verb map (e.g., {:get home-page}
;; apply to / and its children (/about).
(def common-interceptors [(body-params/body-params) http/html-body])

;; Tabular routes
(def routes #{["/activities" :get (conj common-interceptors `activities-page)]
              ["/recommendations" :get (conj common-interceptors `recommendations-page)]
              ["/about" :get (conj common-interceptors `about-page)]})

;; Map-based routes
;(def routes `{"/" {:interceptors [(body-params/body-params) http/html-body]
;                   :get home-page
;                   "/about" {:get about-page}}})

;; Terse/Vector-based routes
;(def routes
;  `[[["/" {:get home-page}
;      ^:interceptors [(body-params/body-params) http/html-body]
;      ["/about" {:get about-page}]]]])


;; Consumed by carto-challenge.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; ::http/interceptors []
              ::http/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::http/allowed-origins ["scheme://host:port"]

              ;; Tune the Secure Headers
              ;; and specifically the Content Security Policy appropriate to your service/application
              ;; For more information, see: https://content-security-policy.com/
              ;;   See also: https://github.com/pedestal/pedestal/issues/499
              ;;::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
              ;;                                                          :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
              ;;                                                          :frame-ancestors "'none'"}}

              ;; Root for resource interceptor that is available by default.
              ::http/resource-path "/public"

              ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
              ;;  This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
              ::http/type :jetty
              ;;::http/host "localhost"
              ::http/port 8080
              ;; Options to pass to the container (Jetty)
              ::http/container-options {:h2c? true
                                        :h2? false
                                        ;:keystore "test/hp/keystore.jks"
                                        ;:key-password "password"
                                        ;:ssl-port 8443
                                        :ssl? false}})

