(ns maailma.encrypt
  (:require [clojure.edn :as edn]
            [clojure.walk :as walk]
            [clojure.tools.logging :as log])
  (:import [org.jasypt.encryption.pbe StandardPBEStringEncryptor]))

;;
;; decrypt
;;

(defrecord ENC [value]
  Object
  (toString [this] (str "#ENC" (into {} this))))

(def readers {'ENC #(ENC. %)})

(defn- create-encryptor [secret]
  (doto (StandardPBEStringEncryptor.)
    (.setAlgorithm "PBEWITHSHA1ANDDESEDE")
    (.setPassword secret)))

(defn encrypt [secret text]
  "Encrypt text using given secret."
  (.encrypt (create-encryptor secret) text))

(defn decrypt
  "Decrypt text using given secret."
  [secret text]
  (.decrypt (create-encryptor secret) text))

(defn decrypt-map
  "Decrypt any ENC instances in the map."
  [secret m]
  (walk/prewalk
    (fn [x]
      (if (instance? ENC x)
        (if secret
          (decrypt secret (:value x))
          (do
            (log/warn "Encrypted value found but no secret provided.")
            x))
        x))
    m))
