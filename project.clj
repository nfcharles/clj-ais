(defproject ais "0.9.2-SNAPSHOT"
  :description "AIS (Automatic Identification System) decoding library"
  :url "https://github.com/nfcharles/clj-ais.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/data.json "0.2.6"]
		 [org.clojure/data.csv "0.1.3"]
		 [org.clojure/core.async "0.2.374"]
		 [com.taoensso/timbre "4.3.1"]
		 [pjson "0.3.8"]
		 [clj-json "0.5.3"]
		 [clj-time "0.13.0"]]
  :repositories [["releases" "file:////home/ncharles/development/clojure/repos/local"]]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  ;:main ais.resilient_decoder
  ;:aot [ais.resilient_decoder]
  :main ais.decode
  :aot [ais.decode]
  :global-vars {*warn-on-reflection* true}
  :jvm-opts ["-Dcom.sun.management.jmxremote"
             "-Dcom.sun.management.jmxremote.ssl=false"
             "-Dcom.sun.management.jmxremote.authenticate=false"
             "-Dcom.sun.management.jmxremote.port=43210"])
