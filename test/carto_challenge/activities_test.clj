(ns carto-challenge.activities-test
  (:require [clojure.test :refer :all]
            [carto-challenge.activities :as act]
            [java-time :as t]))

(def test-activity-1 {:type "Feature",
                      :geometry {:type "Point",
                                 :coordinates '(-3.7081466 40.4087357)},
                      :properties {:name "El Rastro",
                                   :opening_hours {:mo nil,
                                                   :tu nil,
                                                   :we nil,
                                                   :th nil,
                                                   :fr nil,
                                                   :sa nil,
                                                   :su {:open "09:00", :close "15:00"}},
                                   :hours_spent 2.5,
                                   :category "shopping",
                                   :location "outdoors",
                                   :district "Centro"}})

(def test-activity-2 {:type "Feature",
                      :geometry {:type "Point",
                                 :coordinates '(-3.7144063 40.4173423)},
                      :properties {:name "Palacio Real",
                                   :opening_hours {:mo {:open "10:00", :close "20:00"},
                                                   :tu {:open "10:00", :close "20:00"},
                                                   :we {:open "10:00", :close "20:00"},
                                                   :th {:open "10:00", :close "20:00"},
                                                   :fr {:open "10:00", :close "20:00"},
                                                   :sa {:open "10:00", :close "20:00"},
                                                   :su {:open "10:00", :close "20:00"}},
                                   :hours_spent 1.5,
                                   :category "cultural",
                                   :location "outdoors",
                                   :district "Centro"}})

(def test-activity-3 {:type "Feature",
                      :geometry {:type "Point",
                                 :coordinates '(-3.7054455 40.4199837)},
                      :properties {:name "Gran Vía",
                                   :opening_hours {:mo {:open "00:00", :close "23:59"},
                                                   :tu {:open "00:00", :close "23:59"},
                                                   :we {:open "00:00", :close "23:59"},
                                                   :th {:open "00:00", :close "23:59"},
                                                   :fr {:open "00:00", :close "23:59"},
                                                   :sa {:open "00:00", :close "23:59"},
                                                   :su {:open "00:00", :close "23:59"}},
                                   :hours_spent 1,
                                   :category "shopping",
                                   :location "outdoors",
                                   :district "Centro"}})

(def test-activities [test-activity-1 test-activity-2 test-activity-3])

(deftest test-find-activities
  (testing "Find activities function. "
    (testing "Return all activities if no exclusions"
      (is (= 3 (count (:features (act/find-activities nil nil nil test-activities))))))
    (testing "We exclude by category"
      (is (= 1 (count (:features (act/find-activities ["shopping"] nil nil test-activities))))))
    (testing "We exclude by multiple categories"
      (is (= 0 (count (:features (act/find-activities ["shopping" "cultural"] nil nil test-activities))))))
    (testing "We exclude by location"
      (is (= 0 (count (:features (act/find-activities nil ["outdoors"] nil test-activities))))))
    (testing "We exclude by district"
      (is (= 0 (count (:features (act/find-activities nil nil ["Centro"] test-activities))))))))

(deftest test-recommendations
  (testing "Recommendations function. "
    (testing "Returns one activity of the same category that is opened in the time range. "
      (is (=  "Palacio Real"
              (get-in
               (act/recommendations "cultural"
                                    "14:00"
                                    "17:00"
                                    (t/local-date 2018 9 2)
                                    test-activities)
               [:properties :name]))))
    (testing "Returns the activity when the searching range is wider than the opening range. "
      (is (=  "El Rastro"
              (get-in
               (act/recommendations "shopping"
                                    "07:00"
                                    "17:00"
                                    (t/local-date 2018 9 2)
                                    test-activities)
               [:properties :name]))))
    (testing "Returns the longest one when there is more than one available option. "
      (is (= "El Rastro"
             (get-in
              (act/recommendations "shopping"
                                   "10:00"
                                   "14:00"
                                   (t/local-date 2018 9 2)
                                   test-activities)
              [:properties :name]))))
    (testing "Doesn't return an activity if we don't have enough time to the visit because our range"
      (is (= "Gran Vía"
             (get-in (act/recommendations "shopping"
                                          "10:00"
                                          "12:00"
                                          (t/local-date 2018 9 2)
                                          test-activities)
                     [:properties :name]))))
    (testing "Doesn't return an activity if we don't have enough time to the visit because the closing time"
      (is (= "Gran Vía"
             (get-in (act/recommendations "shopping"
                                          "14:00"
                                          "19:00"
                                          (t/local-date 2018 9 2)
                                          test-activities)
                     [:properties :name]))))))

