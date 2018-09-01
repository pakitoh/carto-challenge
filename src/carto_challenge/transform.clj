(ns carto-challenge.transform
  (:require [clojure.string :as s]))

(defn- extract-hours [value]
  (if (not-empty value)
    (let [hours (s/split (first value) #"-")]
      {:open  (get hours 0)
       :close (get hours 1)})))

(defn- transform-hours [opening-hours]
  (reduce conj {} (map (fn [day] {day (extract-hours (day opening-hours))})
                       (keys opening-hours))))

(defn transform [act]
  (let [name (:name act)
        opening-hours (:opening_hours act)
        hours-spent (:hours_spent act)
        category (:category act)
        location (:location act)
        district (:district act)
        coordinates (:latlng act)]
    {:type "Feature"
     :geometry {:type "Point"
                :coordinates (reverse coordinates)}
     :properties {:name name
                  :opening_hours (transform-hours opening-hours)
                  :hours_spent hours-spent
                  :category category
                  :location location
                  :district district}}))

