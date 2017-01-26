(ns toolbelt.event)

(defn value [event]
  (.. event -currentTarget -value))

(defn checked [event]
  (.. event -currentTarget -checked))
