(ns toolbelt.datomic
  (:require [datomic.api :as d]
            [toolbelt.predicates :as p]
            [clojure.spec :as s]))

(defn updated-at
  "Produce the instant in which `entity` was last updated."
  [conn entity]
  (d/q '[:find (max ?tx-time) .
         :in $ ?e
         :where
         [?e _ _ ?tx _]
         [?tx :db/txInstant ?tx-time]]
       (d/history (d/db conn)) (:db/id entity)))

(s/fdef updated-at
        :args (s/cat :connection p/conn? :entity p/entity?)
        :ret inst?)

(defn created-at
  "Produce the instant in which `entity` was created."
  [conn entity]
  (d/q '[:find (min ?tx-time) .
         :in $ ?e
         :where
         [?e _ _ ?tx _]
         [?tx :db/txInstant ?tx-time]]
       (d/history (d/db conn)) (:db/id entity)))

(s/fdef created-at
        :args (s/cat :connection p/conn? :entity p/entity?)
        :ret inst?)
