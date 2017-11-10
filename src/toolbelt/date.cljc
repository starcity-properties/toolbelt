(ns toolbelt.date
  #?(:cljs (:require [cljs-time.format :as f]
                     [cljs-time.core :as t]
                     [cljs-time.coerce :as c]
                     [cljs.spec :as s])
     :clj (:require [clj-time.core :as t]
                    [clj-time.format :as f]
                    [clj-time.coerce :as c]
                    [clojure.spec :as s])))


;; =============================================================================
;; Formatting
;; =============================================================================


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


;; =============================================================================
;; Predicates
;; =============================================================================


(defn is-first-day-of-month? [dt]
  (= (t/day dt) 1))


;; =============================================================================
;; Transformations
;; =============================================================================


#?(:clj
   (do
     (defn to-utc-corrected-date-time
       "Given a DateTime in UTC and `timezone`, produce the datetime in UTC in `timezone`."
       [dt tz]
       (t/to-time-zone (t/from-time-zone dt tz) t/utc))

     (defn to-utc-corrected-date
       "Given an instant in UTC and `timezone`, produce the instant in UTC in `timezone`."
       [inst tz]
       (c/to-date (to-utc-corrected-date-time (c/to-date-time inst) tz)))

     (defn from-tz-date-time
       "Given a DateTime in UTC and `timezone`, produce the datetime in UTC in `timezone`."
       [dt tz]
       (t/from-time-zone (t/to-time-zone dt tz) t/utc))

     (defn from-tz-date
       "Given an instant in UTC and `timezone`, produce the instant in UTC in `timezone`."
       [inst tz]
       (c/to-date (from-tz-date-time (c/to-date-time inst) tz)))

     (defn end-of-day
       "Produce a date that is on the same day as `date`, but with time set to the
        last second in `date`."
       ([date]
        (end-of-day date t/utc))
       ([date tz]
        (let [[y m d] ((juxt t/year t/month t/day) (c/to-date-time date))]
          (-> (t/date-time y m d)
              (t/plus (t/days 1))
              (t/minus (t/seconds 1))
              (c/to-date)
              (to-utc-corrected-date tz)))))

     (defn beginning-of-day
       "Produce a date that is on the same day as `date`, but with time set to the
        first second in `date`."
       ([date]
        (beginning-of-day date t/utc))
       ([date tz]
        (-> (c/to-date-time date)
            (t/floor t/day)
            (c/to-date)
            (to-utc-corrected-date tz))))

     (defn beginning-of-month
       ([date]
        (beginning-of-month date t/utc))
       ([date tz]
        (-> date
            c/from-date
            t/first-day-of-the-month
            (beginning-of-day tz))))

     (defn end-of-month
       ([date]
        (end-of-month date t/utc))
       ([date tz]
        (-> date
            c/from-date
            t/last-day-of-the-month
            (end-of-day tz))))))
