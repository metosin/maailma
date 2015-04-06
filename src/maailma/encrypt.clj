(ns maailma.encrypt
  (:require [clojure.edn :as edn]
            [clojure.walk :as walk])
  (:import [org.jasypt.encryption.pbe StandardPBEStringEncryptor]))

;;
;; decrypt
;;

(defrecord ENC [value]
  Object
  (toString [this] (str "#ENC" (into {} this))))

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
      (if (and secret (instance? ENC x))
        (decrypt secret (:value x))
        x))
    m))
