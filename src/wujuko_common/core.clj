(ns wujuko-common.core
  (:require [clojure.string        :as s]
            [clojure.tools.cli     :as cli]
            [clojure.java.io       :as io]
            [clojure.tools.logging :as log]))

(defn update-vals
  "Updates multiple values inside the map, by using (update-in)"
  [map vals f]
  (reduce (fn [x y] (update-in x [y] f))
          map
          vals))

(defn to-int
  "to-int function, takes optional default."
  [s & [d]] (if (number? s)
              s
              (try (Integer/parseInt s) (catch Exception e d))))

