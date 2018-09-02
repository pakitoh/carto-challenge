(ns carto-challenge.activities
  (:require [carto-challenge.data :as data]
            [carto-challenge.transform :as tr]
            [clojure.string :as s]
            [java-time :as t]))

(def activities
  (map tr/transform data/activities))

(defn- exclude-filter [key-to-exclude excluded-values activity]
  (let [value (get-in activity [:properties key-to-exclude])]
    (not-any? #(= value %) excluded-values)))

(defn- compose-filters [filters]
  (if (empty? filters)
    identity
    (apply every-pred filters)))

(defn- filter-activities [filters activities]
  (filter (compose-filters filters) activities))

(defn find-activities [categories-to-exclude
                       locations-to-exclude
                       districts-to-exclude
                       activities]
  (let [exclude-by-category-filter (fn [activity] (exclude-filter :category categories-to-exclude activity))
        exclude-by-location-filter (fn [activity] (exclude-filter :location locations-to-exclude activity))
        exclude-by-district-filter (fn [activity] (exclude-filter :district districts-to-exclude activity))
        filters [exclude-by-category-filter exclude-by-location-filter exclude-by-district-filter]]
    (filter-activities filters activities)))

(def days {1 :mo
           2 :tu
           3 :we
           4 :th
           5 :fr
           6 :sa
           7 :su})

(defn- filter-by-time-when-is-opened [start-time end-time date activity opening-hours]
  (let [searching-from (t/local-time start-time)
        searching-to   (t/local-time end-time)
        opened-at      (t/local-time (:open opening-hours))
        closed-at      (t/local-time (:close opening-hours))
        hours-spent    (get-in activity [:properties :hours_spent])]
    (and (t/after? searching-from opened-at)
         (t/before? searching-to closed-at)
         (t/before? (t/plus (t/max searching-from opened-at) (t/hours hours-spent))
                    (t/min searching-to closed-at)))))

(defn- filter-by-time [start-time end-time date activity]
  (if-let [opening-hours (get-in activity [:properties :opening_hours (days (t/as date :day-of-week))])]
    (filter-by-time-when-is-opened start-time end-time date activity opening-hours)
    false))

(defn recommendations [category start-time end-time date activities]
  (let [include-category-filter (fn [activity] (= category (get-in activity [:properties :category])))
        time-filter             (fn [activity] (filter-by-time start-time end-time date activity))
        filters                 [include-category-filter time-filter]
        more-time-spent-sorter  (fn [activity] (get-in activity [:properties :hours_spent]))]
    (->> activities
         (filter (compose-filters filters))
         (sort-by more-time-spent-sorter >)
         (first))))

