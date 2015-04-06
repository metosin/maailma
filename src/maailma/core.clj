(ns maailma.core
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.walk :as walk]
            [schema.utils :as su]
            [schema.coerce :as sc]
            [potpuri.core :refer [deep-merge]])
  (:import [java.io File]
           [java.net URL]
           [org.jasypt.encryption.pbe StandardPBEStringEncryptor]))

;;
;; decrypt
;;

(defrecord ENC [value]
  Object
  (toString [this] (str "#ENC" (into {} this))))

(defn- read-edn [s]
  (edn/read-string {:readers {'ENC map->ENC}} s))

(defn- create-encryptor [secret]
  (doto (StandardPBEStringEncryptor.)
    (.setAlgorithm "PBEWITHSHA1ANDDESEDE")
    (.setPassword secret)))

(defn encrypt [secret text]
  (.encrypt (create-encryptor secret) text))

(defn decrypt [secret text]
  (.decrypt (create-encryptor secret) text))

(defn- decrypt-map [secret m]
  (walk/prewalk
    (fn [x]
      (if (and secret (instance? ENC x))
        (decrypt secret (:value x))
        x))
    m))

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
  [config prefix properties]
  (reduce (fn [acc [k v]]
            (if-let [ks (->ks prefix k)]
              (assoc-in acc ks v)
              acc))
          config properties))

(defn read-env-file
  "Read config from given File or URL.
   Non-existing files are skipped."
  [config env-file]
  (if (or (and (instance? File env-file) (.exists env-file)) (instance? URL env-file))
    (deep-merge config (-> env-file slurp read-edn))
    config))

(defn env-coerce! [value schema]
  (let [coercer (sc/coercer schema sc/string-coercion-matcher)
        coerced (coercer value)]
    (if (su/error? coerced)
      (throw (ex-info (format "Env value coercion failed: %s" value) {:error (:error coerced)}))
      coerced)))

(defn env-get
  ([config ks]
   (get-in config ks))
  ([config ks schema]
   (env-coerce! (get-in config ks) schema)))

(defn read-config!
  "Read and merge config from several sources:

   - config-defaults.edn resource
   - envinronment variables (filtered by prefix)
   - system properties (filtered by prefix)
   - {prefix}-private.edn file in current directory
   - config-local.edn file in current directory
   - override parameter

   Special property `:private-key` will be used to
   decrypt any encrypted (#ENC tag) values."
  [prefix & [override]]
  (let [config (-> {}
                   (read-env-file (io/resource "config-defaults.edn"))
                   (read-system prefix (System/getenv))
                   (read-system prefix (System/getProperties))
                   (read-env-file (io/file (str "./" prefix "-private.edn")))
                   (read-env-file (io/file "./config-local.edn"))
                   (deep-merge (or override {})))
        private-key (:private-key config)
        config (dissoc config :private-key)
        decrypted (decrypt-map private-key config)]
    decrypted))
