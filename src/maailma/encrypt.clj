(ns maailma.encrypt
  (:require [clojure.edn :as edn]
            [clojure.walk :as walk]
            [clojure.tools.logging :as log])
  (:import [org.jasypt.encryption.pbe StandardPBEStringEncryptor]
           [org.jasypt.registry AlgorithmRegistry]))


(def +default-algorithm+ "PBEWITHSHA1ANDDESEDE")

(defrecord ENC [algorithm value])

(defmethod print-method ENC [v out]
  (.write out (str "#ENC" (pr-str (into {} v)))))

(defmethod print-dup ENC [v out]
  (.write out (str "#ENC" (pr-str (into {} v)))))

(defn- merge-default-options [options]
  (-> options
      (update :algorithm #(or % +default-algorithm+))))

(defn enc
  "Creates a new ENC record."
  [m]
  {:pre [(string? (:algorithm m))]}
  (map->ENC m))

(defn read-enc
  "Reads tagged value from EDN.

  Supports both maps #ENC{:value \"encoded-string\" :algorithm \"foo\"}
  and strings #ENC\"encoded-string\"."
  [map-or-string]
  (if (string? map-or-string)
    ;; Legacy format
    (enc {:algorithm +default-algorithm+ :value map-or-string})
    (enc map-or-string)))

(def readers {'ENC read-enc})

(defn- create-encryptor [secret {:keys [algorithm]}]
  ;; TODO: Other options? keyObtentionIterations?
  (doto (StandardPBEStringEncryptor.)
    (.setAlgorithm (or algorithm +default-algorithm+))
    (.setPassword secret)))

(defn encrypt
  "Encrypt text using given secret.

  Returns ENC Record. Use pr-str to turn this into string for the EDN file."
  ([secret text] (encrypt secret text nil))
  ([secret text options]
   (let [options (merge-default-options options)]
     (enc (assoc options :value (.encrypt (create-encryptor secret options) text))))))

(defn decrypt
  "Decrypt value from given ENC record using given secret."
  [secret enc]
  {:pre [(instance? ENC enc)]}
  (.decrypt (create-encryptor secret (dissoc enc :value)) (:value enc)))

(defn decrypt-map
  "Decrypt any ENC instances in the map."
  [secret m]
  (walk/prewalk
    (fn [x]
      (if (instance? ENC x)
        (if secret
          (decrypt secret x)
          (do
            (log/warn "Encrypted value found but no secret provided.")
            x))
        x))
    m))

(defn available-algorithms
  "Returns list of algorithms that can be used. Check Maailma README
  for notes on how to enable more algorithms."
  []
  (for [algorithm (AlgorithmRegistry/getAllPBEAlgorithms)
        :let [e (create-encryptor "secret" {:algorithm algorithm})]
        :when (try
                (= "value" (.decrypt e (.encrypt e "value")))
                (catch Exception e
                  false))]
    algorithm))

(comment
  (available-algorithms))
