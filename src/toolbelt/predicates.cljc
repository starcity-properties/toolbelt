(ns toolbelt.predicates
  #?(:cljs (:require [clojure.core.async])
     :clj (:require [clojure.core.async]
                    [datomic.api]
                    [toolbelt.datomic])))

#?(:clj
   (do
     (defn entity?
       "Is `x` an EntityReference?"
       [x]
       (toolbelt.datomic/entity? x))

     (defn entityd?
       "Is `x` a Datomic entity?"
       [x]
       (toolbelt.datomic/entityd? x))

     (defn conn?
       "Is `x` a Datomic connection?"
       [x]
       (instance? datomic.Connection x))

     (defn db?
       "Is `x` a Datomic database?"
       [x]
       (toolbelt.datomic/db? x))

     (defn lookup?
       "is `x` a lookup ref?"
       [x]
       (and (vector? x)
            (keyword? (first x))
            (= (count x) 2)))))

(defn throwable? [x]
  (instance? #?(:clj java.lang.Throwable
                :cljs js/Error)
             x))

(defn chan? [x]
  (satisfies? clojure.core.async.impl.protocols/Channel x))
