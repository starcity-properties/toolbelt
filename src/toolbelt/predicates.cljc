(ns toolbelt.predicates
  (:require clojure.core.async
            #?(:clj datomic.api)))

#?(:clj
   (defn entity?
     "Is `x` a Datomic entity?"
     [x]
     (= datomic.query.EntityMap (type x)))

   (defn conn?
     "Is `x` a Datomic connection?"
     [x]
     (= datomic.peer.LocalConnection (type x)))

   (defn lookup?
     "is `x` a lookup ref?"
     [x]
     (and (vector? x)
          (keyword? (first x))
          (= (count x) 2))))

(defn throwable? [x]
  (instance? #?(:clj java.lang.Throwable
                :cljs js/Error)
             x))

(defn chan? [x]
  (satisfies? clojure.core.async.impl.protocols/Channel x))
