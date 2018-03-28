(ns maailma.core-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [maailma.core :refer :all])
  (:import [java.io File]))

(deftest ->ks-test
  (is (= [:http :port]
         (->ks "maailma" "MAAILMA_HTTP_PORT")))
  (is (= [:http :port]
         (->ks "maailma" "maailma.http.port")))
  (is (= nil
         (->ks "maailma" "foo.bar"))))

(deftest read-system-test
  (is (= {:http {:port "3000"}}
         (read-system "maailma" {"java.class.path" "" "maailma.http.port" "3000"}))))

(def test-file (File/createTempFile "maailma" "config-test.edn"))
(spit test-file {:http {:port 3000}})

(deftest read-env-file-test
  (is (= {:http {:port 3000}}
         (read-env-file test-file))))

(deftest read-config!-test
  (let [config (read-config! "maailma" {:db {:port-number 5433}})]
    (testing "./config-local.edn"
      (is (true? (get-in config [:local]))))
    (testing "test-file"
      (is (= 3000 (get-in config [:http :port]))))
    (testing "config-defaults.edn resource"
      (is (= "localhost" (get-in config [:db :server-name]))))
    (testing "override"
      (is (= 5433 (get-in config [:db :port-number]))))
    (testing "db password value"
      (is (= "abc123" (get-in config [:db :password]))))))

(defrecord Foo [x])

(deftest edn-readers-test
  (let [config (resource "config-readers.edn" {:readers {'m/foo ->Foo}})]
    (testing "./config-local.edn"
      (is (instance? Foo (:asd config))))))
