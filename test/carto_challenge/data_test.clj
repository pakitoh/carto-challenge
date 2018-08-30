(ns carto-challenge.data-test
  (:require [clojure.test :refer :all]
            [carto-challenge.data :as d]
            [clojure.java.io :as io]))

(deftest load-data
  (testing "Integration tests for the data namespace. "
    (testing "There is a folder for the data files"
      (is (= "data" d/folder)))
    (testing "We can find the example datafile in folder"
      (is (= 1 (count (d/files d/folder))))
      (is (= "madrid.json" (.getName (first (d/files d/folder))))))
    (testing "We can get the content of the example datafile as EDN"
      (is (not (nil? (d/load (first (d/files d/folder)))))))
    (testing "Activities has been loaded"
      (is (not (nil? d/activities))))))

