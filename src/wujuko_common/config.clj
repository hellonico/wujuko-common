(ns wujuko-common.config
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log])
  (:import [java.io PushbackReader]))

;;
;; Rebind these for testing or alternate us.
(def ^:dynamic *default-configuration-location* "/wujuko/wujuko-config/")
(def ^:dynamic *default-configuration-file* "wujuko.clj")
(def ^:dynamic *default-security-file* "wujuko-secure.clj")

(defn- get-config
  "Given file 'f', returns the Clojure data structure representing the
   configuration for the application."
  [f]
  (with-open [r (io/reader f)]
    (read (PushbackReader. r))))

(defn- get-secure
  [k f]
  (k (get-config f)))

(defn alter-config-locations
  [& {:keys [root fileprefix] :or {root "/wujuko/wujuko-config/" fileprefix "wujuko"}}]
  (def ^:dynamic *default-configuration-location* root)
  (def ^:dynamic *default-configuration-file* (str fileprefix ".clj"))
  (def ^:dynamic *default-security-file* (str fileprefix "-secure.clj")))

;; For a given applications position in the map, we take
;; the non-secure map at key k and merge the secure
;; configuration into it.
;;
;; The non-secure configuration is suitable for revision control.
;;
(defn get-config-for-app
  "Returns the configuration map for the provided :key."
  [k]
  (let [f *default-configuration-location*
        rpath (str f *default-configuration-file*)
        spath (str f *default-security-file*)]
    (log/info "Loading configuration from: " rpath)
    (log/info "Loading secure config from: " spath)
    (merge (k (get-config rpath)) (get-secure k spath))))