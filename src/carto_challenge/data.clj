(ns carto-challenge.data
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]))

(def folder "data")

(defn files [folder]
  (-> folder
      (io/resource)
      (io/file)
      (.listFiles)))

(def data-files
  (files folder))

(defn load-datafile [datafile]
  (-> datafile
      (slurp)
      (json/read-str :key-fn keyword)))

(def activities
  (mapcat load-datafile data-files))

