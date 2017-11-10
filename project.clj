(defproject starcity/toolbelt "0.1.11"
  :description "Library of utility functions for Starcity projects."
  :url "https://github.com/starcity-properties/toolbelt"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha14"]
                 [com.datomic/datomic-free "0.9.5544"]
                 [org.clojure/core.async "0.2.395"]
                 [re-frame "0.9.1"]
                 [reagent "0.6.0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [clj-time "0.12.0"]
                 [bouncer "1.0.1"]]

  :plugins [[s3-wagon-private "1.2.0"]]

  :repositories {"releases" {:url        "s3://starjars/releases"
                             :username   :env/aws_access_key
                             :passphrase :env/aws_secret_key}} )
