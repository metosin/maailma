(ns maailma.encrypt-test
  (:require [clojure.test :refer :all]
            [maailma.encrypt :refer :all]
            [maailma.core :as core]))

(deftest encrypt-test
  (testing "default algorithm"
    (let [secret "salasana"
          data "secret-stuff"
          enc (encrypt secret data)]
      (is (= 32 (count (:value enc))))
      (is (not= data enc))
      (is (= data (decrypt secret enc)))))

  (testing "worse algorithm"
    (let [secret "salasana"
          data "foobar"
          enc (encrypt secret data {:algorithm "PBEWITHMD5ANDDES"})]
      (is (= 24 (count (:value enc))))
      (is (not= data enc))
      (is (= data (decrypt secret enc))))))

(deftest read-edn-test
  (let [data {:password (encrypt "secret" "value")}
        edn (pr-str data)]
    (is (= data (#'core/read-edn edn)))
    (is (= {:password "value"} (decrypt-map "secret" (#'core/read-edn edn))))))

(deftest e2e-test
  (let [f (doto (java.io.File/createTempFile "maailma" ".edn")
            (.deleteOnExit))]
    (spit f "{:password #ENC{:algorithm \"PBEWITHSHA1ANDDESEDE\", :value \"7ILc1uTatombQl/ovo869w==\"}}")
    (is (= {:password "value"}
           (core/build-config
             (core/file (.getPath f))
             {:private-key "secret"})))))

(deftest read-legacy-edn-test
  (testing "EDN where #ENC value is string instead of map"
    (let [edn "{:password #ENC\"VqBbumymZHwi5B8tUKOVhA==\"}"
          data (#'core/read-edn edn)]
      (is (= {:password (->ENC nil "VqBbumymZHwi5B8tUKOVhA==")} data))
      (is (= {:password "value"} (decrypt-map "secret" data))))))
