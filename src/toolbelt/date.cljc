(ns toolbelt.date
  #?(:cljs (:require [cljs-time.format :as f]
                     [cljs-time.core :as t]
                     [cljs-time.coerce :as c]
                     [cljs.spec :as s])
     :clj (:require [clj-time.format :as f]
                    [clj-time.coerce :as c]
                    [clojure.spec :as s])))

(def ^:private formatters
  {:short-date      (f/formatter "M/d/yy")
   :short-date-time (f/formatter "M/d/yy, h:mma")})

(defn short-date [date]
  (f/unparse (:short-date formatters)
             #?(:cljs (t/to-default-time-zone date)
                :clj (c/to-date-time date))))

(s/fdef short-date
        :args (s/cat :date inst?)
        :ret string?)

(defn short-date-time [date]
  (f/unparse (:short-date-time formatters)
             #?(:cljs (t/to-default-time-zone date)
                :clj (c/to-date-time date))))

(s/fdef short-date-time
        :args (s/cat :date inst?)
        :ret string?)
