(ns carto-challenge.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.test :refer :all]
            [io.pedestal.http :as bootstrap]
            [carto-challenge.service :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(deftest about-page-test
  (is (.contains
       (:body (response-for service :get "/about"))
       "Clojure 1.9"))
  (is (=
       {"Content-Type" "text/html;charset=UTF-8"
        "Strict-Transport-Security" "max-age=31536000; includeSubdomains"
        "X-Frame-Options" "DENY"
        "X-Content-Type-Options" "nosniff"
        "X-XSS-Protection" "1; mode=block"
        "X-Download-Options" "noopen"
        "X-Permitted-Cross-Domain-Policies" "none"
        "Content-Security-Policy" "object-src 'none'; script-src 'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:;"}
       (:headers (response-for service :get "/about")))))


(deftest activities-page-test
  (is (= ;.contains
     "[{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-3.6834559,40.4154589]},\"properties\":{\"name\":\"El Retiro\",\"opening_hours\":{\"mo\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"tu\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"we\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"th\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"fr\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"sa\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"su\":{\"open\":\"00:00\",\"close\":\"23:59\"}},\"hours_spent\":1.5,\"category\":\"nature\",\"location\":\"outdoors\",\"district\":\"Retiro\"}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-3.7481842,40.4202185]},\"properties\":{\"name\":\"Casa de Campo\",\"opening_hours\":{\"mo\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"tu\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"we\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"th\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"fr\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"sa\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"su\":{\"open\":\"00:00\",\"close\":\"23:59\"}},\"hours_spent\":3,\"category\":\"nature\",\"location\":\"outdoors\",\"district\":\"Latina\"}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-3.6932923,40.4471904]},\"properties\":{\"name\":\"ECI Nuevos Ministerios\",\"opening_hours\":{\"mo\":{\"open\":\"10:00\",\"close\":\"22:00\"},\"tu\":{\"open\":\"10:00\",\"close\":\"22:00\"},\"we\":{\"open\":\"10:00\",\"close\":\"22:00\"},\"th\":{\"open\":\"10:00\",\"close\":\"22:00\"},\"fr\":{\"open\":\"10:00\",\"close\":\"22:00\"},\"sa\":{\"open\":\"10:00\",\"close\":\"22:00\"},\"su\":{\"open\":\"10:00\",\"close\":\"22:00\"}},\"hours_spent\":2,\"category\":\"shopping\",\"location\":\"indoors\",\"district\":\"Chamber\\u00ed\"}},{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[-3.7223896,40.4273119]},\"properties\":{\"name\":\"Parque del Oeste\",\"opening_hours\":{\"mo\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"tu\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"we\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"th\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"fr\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"sa\":{\"open\":\"00:00\",\"close\":\"23:59\"},\"su\":{\"open\":\"00:00\",\"close\":\"23:59\"}},\"hours_spent\":1,\"category\":\"nature\",\"location\":\"outdoors\",\"district\":\"Chamber\\u00ed\"}}]"
       (:body (response-for service :get "/activities?excudeCategory=nature&excludeCategory=cultural&excludeDistrict=Centro")))))

