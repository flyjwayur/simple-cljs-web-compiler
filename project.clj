(defproject cljs-compiler "0.1.0-SNAPSHOT"
  :description "Eval clojurescript to javascript!"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.omcljs/om "1.0.0-alpha22"]]
  :plugins [[lein-figwheel "0.5.0-6"]
            [lein-cljsbuild "1.1.2" :exclusions [[org.clojure/clojure]]]]
  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel true
                :compiler {
                  :main cljs-compiler.core
                  :asset-path "js"
                  :output-to "resources/public/js/main.js"
                  :output-dir "resources/public/js"}}]})
