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

(defn sorted-map-by-values
  "Returns a map sorted by values given the input map.
   'm' A map to sort.
   Optional 'reverse': Boolean for reverse sorting order"
  [m & [reverse]]
  (into (sorted-map-by
         (fn [k1 k2]
           (let [cval (compare [(get m k1) k1]
                               [(get m k2) k2])]
             (if reverse (* -1 cval) cval))))
        m))

;;
;; Taken from:
;; Michal Marczyk's Solution:
;; http://stackoverflow.com/questions/2640169/whats-the-easiest-way-to-parse-numbers-in-clojure
(let [m (.getDeclaredMethod clojure.lang.LispReader
                            "matchNumber"
                            (into-array [String]))]
  (.setAccessible m true)
  (defn parse-number
    "Use the Clojure number matcher to parse numbers."
    [s]
    (.invoke m clojure.lang.LispReader (into-array [s]))))


(defn set-signal-handler!
  "Sets the signal handler for various OS level signals that are sent to
   the JVM. 'f' is a function taking one message parameter. 'sig' is the
   signal to respond to, INT (ctrl-c), TERM (kill)"
  [f sig]
  (sun.misc.Signal/handle
   (sun.misc.Signal. sig)
   (proxy [sun.misc.SignalHandler] []
     (handle [signal]
       (f (str "-- caught signal " signal))))))
