(ns maailma.core
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [potpuri.core :refer [deep-merge]]
            [maailma.encrypt :as enc])
  (:import [java.io File]
           [java.net URL]))

;;
;; common
;;

(defn ->ks
  "Normalize the string, split and map to keywords.
   Returns nil if the string doesn't match the app property prefix."
  [prefix s]
  (some-> (string/lower-case s)
          (string/replace "_" ".")
          (->> (re-find (re-pattern (str "^" prefix "\\.(.*)"))))
          second
          (string/split #"\.")
          (as-> s (map keyword s))))

(defn read-system
  [prefix properties]
  (reduce (fn [acc [k v]]
            (if-let [ks (->ks prefix k)]
              (assoc-in acc ks v)
              acc))
          {} properties))

(defn- read-edn [s]
  (edn/read-string {:readers enc/readers} s))

(defn read-env-file
  "Read config from given File or URL.
   Non-existing files are skipped."
  [env-file]
  (if (or (and (instance? File env-file) (.exists env-file))
          (instance? URL env-file))
    (-> env-file slurp read-edn)))

(defn read-config!
  "Read and merge config from several sources:

   - config-defaults.edn resource
   - envinronment variables (filtered by prefix)
   - system properties (filtered by prefix)
   - config-local.edn file in current directory
   - override parameter

   Special property `:private-key` will be used to
   decrypt any encrypted (#ENC tag) values."
  [prefix & [override]]
  (let [config (deep-merge
                 (read-env-file (io/resource "config-defaults.edn"))
                 (read-system prefix (System/getenv))
                 (read-system prefix (System/getProperties))
                 (read-env-file (io/file "./config-local.edn"))
                 override)
        private-key (:private-key config)
        config (dissoc config :private-key)
        decrypted (enc/decrypt-map private-key config)]
    decrypted))
