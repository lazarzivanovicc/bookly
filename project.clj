(defproject bookly "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.1"]
                 [ring/ring-defaults "0.3.2"]
                 [midje "1.10.9"]
                 [org.clojure/data.csv "1.1.0"]
                 [ring/ring-json "0.3.1"]]
  :plugins [[lein-ring "0.12.5"]]
  :ring {:handler bookly.core/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.2"]]}})
