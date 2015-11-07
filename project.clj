(defproject metosin/maailma "0.2.0"
  :description "Metosin maailma"
  :url "https://github.com/metosin/maailma"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :dependencies [[org.jasypt/jasypt "1.9.2"]
                 [metosin/potpuri "0.2.3"]
                 [org.clojure/tools.logging "0.3.1"]]
  :plugins [[lein-codox "0.9.0"]]

  :codox {:source-uri "http://github.com/metosin/maailma/blob/master/{filepath}#L{line}"}

  :profiles {:dev {:plugins [[jonase/eastwood "0.2.1"]]
                   :dependencies [[criterium "0.4.3"]
                                  [org.clojure/clojure "1.7.0"]]
                   :resource-paths ["test-resources"]}
             :1.6 {:dependencies [[org.clojure/clojure "1.6.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0-beta2"]]}}
  :aliases {"all" ["with-profile" "dev:dev,1.6:dev,1.8"]
            "test-clj"  ["all" "do" ["test"] ["check"]]})
