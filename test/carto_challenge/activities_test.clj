(ns carto-challenge.activities-test
  (:require [clojure.test :refer :all]
            [carto-challenge.activities :as act]))

(def activity {:name "El Rastro",
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

(def feature {:type "Feature",
              :geometry {:type "Point",
                         :coordinates [-3.7081466 40.4087357]},
              :properties {:name "El Rastro",
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
                           :district "Centro"}})

(deftest test-transform
  (testing "Tests for transform function. "
    (testing "We can transform one activity into one GeoJSON feature"
      (is (= feature (act/transform activity))))
    (testing "Integration test that checks that the datafile content has been converted"
      (is (= 10 (count act/features))))))

(deftest test-exclude
  (testing "Exclude function. "
    (testing "Return true when feature is not going to be filtered out because category is not among the excluded"
      (is (= true   (act/exclude :category ["cultural"] feature))))
    (testing "Return false when category is among those that we want to exclude"
      (is (= false  (act/exclude :category ["shopping"] feature))))
    (testing "Return false when category is among those that we want to exclude but not the first one"
      (is (= false  (act/exclude :category ["nature" "shopping"] feature))))))

(deftest test-filters
  (testing "Exclusion filters logic. "
    (testing "If no filter we return all activities"
      (is (= 1 (count (act/filter-activities [] [feature])))))
    (testing "Return all activities when no filter out with filters"
      (is (= 0 (count (act/filter-activities [#(act/exclude :category ["shopping"] %)] [feature])))))
    (testing "Empty response when filter out by category shopping"
      (is (= 0 (count (act/filter-activities [#(act/exclude :category ["shopping"] %)] [feature])))))
    (testing "Empty response when filter out by category shopping when several filters"
      (is (= 0 (count (act/filter-activities [#(act/exclude :location ["indoors"] %)
                                              #(act/exclude :category ["shopping"] %)]
                                             [feature])))))))

(deftest test-find-features
  (testing "Find features function. "
    (testing "Return all features if no exclusions"
      (is (= 1 (count (act/find-features nil nil nil [feature])))))
    (testing "We exclude by category"
      (is (= 0 (count (act/find-features ["shopping"] nil nil [feature])))))
    (testing "We exclude by location"
      (is (= 0 (count (act/find-features nil ["outdoors"] nil [feature])))))
    (testing "We exclude by district"
      (is (= 0 (count (act/find-features nil nil ["Centro"] [feature])))))))


