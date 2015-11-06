(defproject metosin/maailma "0.2.0-SNAPSHOT"
  :description "Metosin maailma"
  :url "https://github.com/metosin/maailma"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [prismatic/schema "0.4.2"]
                 [org.jasypt/jasypt "1.9.2"]
                 [metosin/potpuri "0.2.2"]]
  :plugins [[codox "0.8.10"]]

  :codox {:src-dir-uri "http://github.com/metosin/maailma/blob/master/"
          :src-linenum-anchor-prefix "L"}

  :profiles {:dev {:plugins [[jonase/eastwood "0.2.1"]]
                   :dependencies [[criterium "0.4.3"]
                                  [org.clojure/clojurescript "0.0-3126"]]
                   :resource-paths ["test-resources"]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0-alpha6"]]}}
  :aliases {"all" ["with-profile" "dev:dev,1.7"]
            "test-clj"  ["all" "do" ["test"] ["check"]]})
