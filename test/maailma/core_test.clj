(ns maailma.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [schema.core :as s]
            [maailma.core :refer :all])
  (:import [java.io File]))

(deftest ->ks-test
  (is (= [:http :port]
         (->ks "maailma" "MAAILMA_HTTP_PORT")))
  (is (= [:http :port]
         (->ks "maailma" "maailma.http.port")))
  (is (= nil
         (->ks "maailma" "foo.bar"))))

(deftest env-get-test
  (is (= 5 (env-get {:a "5"} [:a] s/Num))))

(deftest read-system-test
  (is (= {:http {:port "3000"}}
         (read-system {} "maailma" {"java.class.path" "" "maailma.http.port" "3000"}))))

(def test-file (File/createTempFile "maailma" "config-test.edn"))
(spit test-file {:http {:port 3000}})

(deftest read-env-file-test
  (is (= {:http {:port 3000}}
         (read-env-file {} test-file))))

(deftest read-config!-test
  (let [config (read-config! "maailma" {:db {:port-number 5433}})]
    (testing "./config-local.edn"
      (is (true? (env-get config [:local]))))
    (testing "test-file"
      (is (= 3000 (env-get config [:http :port] s/Num))))
    (testing "config-defaults.edn resource"
      (is (= "localhost" (env-get config [:db :server-name]))))
    (testing "override"
      (is (= 5433 (env-get config [:db :port-number]))))
    (testing "decrypred value"
      (is (= "abc123" (env-get config [:db :password]))))))
