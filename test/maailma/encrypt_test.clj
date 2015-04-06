(ns maailma.core-test
  (:require [clojure.test :refer :all]
            [maailma.encypt :refer :all]))

(deftest encrypt-test
  (let [secret "salasana"
        data "secret-stuff"
        enc (encrypt secret data)]
    (is (not= data enc))
    (is (= data (decrypt secret enc)))))
