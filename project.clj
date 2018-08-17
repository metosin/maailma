(defproject metosin/maailma "1.1.1-SNAPSHOT"
  :description "An opinionated configuration library"
  :url "https://github.com/metosin/maailma"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v20.html"}
  :dependencies [[metosin/potpuri "0.5.1"]]
  :plugins [[lein-codox "0.10.3"]]

  :codox {:source-uri "http://github.com/metosin/maailma/blob/{version}/{filepath}#L{line}"}

  :profiles {:dev {:plugins [[jonase/eastwood "0.2.1"]]
                   :dependencies [[criterium "0.4.4"]
                                  [org.clojure/clojure "1.9.0"]]
                   :resource-paths ["test-resources"]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  :aliases {"all" ["with-profile" "dev:dev,1.7:dev,1.8"]
            "test-clj"  ["all" "do" ["test"] ["check"]]})
