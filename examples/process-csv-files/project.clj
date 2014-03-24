(defproject ef-example-process-csv-files "0.0.1"
  :source-paths ["src"]
  :aot :all
  :main ef-example.process-csv-files
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.csv "0.1.2"]
                 [cheshire "5.3.1"]
                 [clj-time "0.6.0"]
                 [event-fabric/api-client "0.1.2-SNAPSHOT"]
                 [com.fasterxml.jackson.core/jackson-databind "2.3.2"]
                 [org.clojure/tools.logging "0.2.6"]]
  :profiles {:dev {:dependencies []}}
  :min-lein-version "2.0.0")
