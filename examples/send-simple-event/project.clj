(defproject ef-example-send-simple-event "0.0.1"
  :source-paths ["src"]
  :aot :all
  :main ef-example.send-simple-event
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [cheshire "5.3.1"]
                 [event-fabric/api-client "0.2.0-SNAPSHOT"]
                 [com.fasterxml.jackson.core/jackson-databind "2.3.2"]
                 [org.clojure/tools.logging "0.2.6"]]
  :profiles {:dev {:dependencies []}}
  :min-lein-version "2.0.0")
