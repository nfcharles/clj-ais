(defproject ais "0.8.0-SNAPSHOT"
  :description "AIS (Automatic Identification System) decoding library"
  :url "https://github.com/nfcharles/clj-ais.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.6"]
		 [org.clojure/data.csv "0.1.3"]
		 [org.clojure/core.async "0.2.374"]]
  :repositories [["releases" "file:////home/ncharles/development/clojure/repos/local"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :main ais.resilient_decoder
  :aot [ais.resilient_decoder])
