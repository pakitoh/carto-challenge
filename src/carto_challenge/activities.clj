(ns carto-challenge.activities
  (:require [carto-challenge.data :as d]
            [carto-challenge.activities :as act]))

(defn transform [act]
  {:type "Feature"
   :geometry {:type "Point" :coordinates (reverse (:latlng act))}
   :properties (dissoc act :latlng)})

(def features
  (map transform d/activities))

(defn exclude [key-to-exclude excluded-values feature]
  (let [value (get-in feature [:properties key-to-exclude])]
    (not-any? #(= value %) excluded-values)))

(defn compose-filters [filters]
  (if (empty? filters)
    identity 
    (apply every-pred filters)))

(defn filter-activities [filters features]
  (filter (compose-filters filters) features))







;(def filters [
;              #(act/exclude :location ["indoors"] %)
;              #(act/exclude :category ["shopping"] %)]
;  ])



;(defn exclude-by-category-filter [categories-to-exclude]
;  (fn [feature] (act/exclude :category categories-to-exclude)))



(defn find-features [categories-to-exclude
                     locations-to-exclude
                     districts-to-exclude
                     features]
  (let [filters [#(act/exclude :category categories-to-exclude %)
                 #(act/exclude :location locations-to-exclude %)
                 #(act/exclude :district districts-to-exclude %)]]
        (filter-activities filters features)))



