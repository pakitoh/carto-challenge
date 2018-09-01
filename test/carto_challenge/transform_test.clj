(ns carto-challenge.transform-test
   (:require [clojure.test :refer :all]
             [carto-challenge.transform :as tr]))

(def test-activity-1 {:name "El Rastro",
                      :opening_hours {:mo [],
                                      :tu [],
                                      :we [],
                                      :th [],
                                      :fr [],
                                      :sa [],
                                      :su ["09:00-15:00"]},
                      :hours_spent 2.5,
                      :category "shopping",
                      :location "outdoors",
                      :district "Centro",
                      :latlng [40.4087357 -3.7081466]})

(def test-feature-1 {:type "Feature",
                      :geometry {:type "Point",
                                 :coordinates '(-3.7081466 40.4087357)},
                      :properties {:name "El Rastro",
                                   :opening_hours {:mo nil
                                                   :tu nil
                                                   :we nil
                                                   :th nil
                                                   :fr nil
                                                   :sa nil
                                                   :su {:open "09:00"
                                                        :close "15:00"}},
                                   :hours_spent 2.5,
                                   :category "shopping",
                                   :location "outdoors",
                                   :district "Centro"}})

(deftest test-transform
  (testing "Tests for transform function. "
    (testing "We can transform one activity into one GeoJSON feature"
      (is (= test-feature-1 (tr/transform test-activity-1))))))

