(ns maailma.core
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [potpuri.core :refer [deep-merge]])
  (:import [java.io File]
           [java.net URL]))

;;
;; common
;;

(defn ^:no-doc ->ks
  "Normalize the string, split and map to keywords.
   Returns nil if the string doesn't match the app property prefix."
  [prefix s]
  (some-> (string/lower-case s)
          (string/replace "_" ".")
          (->> (re-find (re-pattern (str "^" prefix "\\.(.*)"))))
          second
          (string/split #"\.")
          (as-> s (map keyword s))))

(defn ^:no-doc read-system
  [prefix properties]
  (reduce (fn [acc [k v]]
            (if-let [ks (->ks prefix k)]
              (assoc-in acc ks v)
              acc))
          {} properties))

(defn- read-edn [s reader-opts]
  (edn/read-string (or reader-opts {}) s))

(defn read-env-file
  "Read config from given File or URL.
   Non-existing files are skipped."
  ([env-file]
   (read-env-file env-file nil))
  ([env-file reader-opts]
   (if (or (and (instance? File env-file) (.exists env-file))
           (instance? URL env-file))
     (-> env-file slurp (read-edn reader-opts)))))

(defn resource
  "Reads configuration part from given path in classpath."
  ([path]
   (resource path nil))
  ([path reader-opts]
   (read-env-file (io/resource path) reader-opts)))

(defn file
  "Reads configuration part from given path in filesystem."
  ([file-path]
   (file file-path nil))
  ([file-path reader-opts]
   (read-env-file (io/file file-path) reader-opts)))

(defn env
  "Reads configuration part from envinronment variables, filtered by a prefix."
  [prefix]
  (read-system prefix (System/getenv)))

(defn properties
  "Reads configuration part from system properties, filtered by a prefix."
  [prefix]
  (read-system prefix (System/getProperties)))

(defn env-var
  "Reads a configuration value from an environment variable and associates it to
  the given path in a map.

      (env-var \"SERVER_PORT\" [:http :port])  ; => {:http {:port \"8080\"}}"
  [var-name path]
  (when-let [value (System/getenv var-name)]
    (assoc-in {} path value)))

(defn build-config
  "Merges given configuration parts."
  [& parts]
  ; Filter nils out, (deep-merge {...} nil) -> nil
  (apply deep-merge (filter identity parts)))

(defn read-config!
  "Read and merge config from several sources:

   - config-defaults.edn resource
   - envinronment variables (filtered by prefix)
   - system properties (filtered by prefix)
   - config-local.edn file in current directory
   - override parameter"
  ([prefix] (read-config! prefix nil))
  ([prefix override]
   (build-config
     (resource "config-defaults.edn")
     (env prefix)
     (properties prefix)
     (file "./config-local.edn")
     override)))
