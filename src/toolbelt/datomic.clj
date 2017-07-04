(ns toolbelt.datomic
  "Credit to Robert Stuttaford: https://gist.github.com/robert-stuttaford/39d43c011e498542bcf8"
  (:require [datomic.api :as d]
            [clojure.spec :as s]))


;; =============================================================================
;; DatabaseReference
;; =============================================================================


(defprotocol DatabaseReference
  (as-db [_] [_ _]))


(def sync-timeout-ms (* 10 1000))


(extend-protocol DatabaseReference
  datomic.db.Db
  (as-db
    ([db] db))
  datomic.Connection
  (as-db
    ([conn] (d/db conn))
    ([conn t] (let [db-value-or-timeout (-> (d/sync conn t)
                                            (deref sync-timeout-ms :timeout))]
                (cond
                  (= (type db-value-or-timeout) datomic.db.Db)
                  db-value-or-timeout

                  (= db-value-or-timeout :timeout)
                  (throw
                   (ex-info "Datomic timed out during sync attempt."
                            {:waited-ms sync-timeout-ms}))

                  :else
                  (throw
                   (ex-info "An unknown error occured while Datomic was trying to sync."
                            {})))))))


;; =============================================================================
;; EntityReference
;; =============================================================================


(defprotocol EntityReference
  (id [_])
  (entity [_ db]))


(extend-protocol EntityReference
  datomic.query.EntityMap
  (id [e] (:db/id e))
  (entity [e db] e)
  java.lang.Long
  (id [id] id)
  (entity [id db] (d/entity (as-db db) id))

  ;; these two are for working with maps composed for a transaction
  clojure.lang.PersistentArrayMap
  (id [m] (:db/id m))
  (entity [m db] (entity (id m) (as-db db)))
  clojure.lang.PersistentHashMap
  (id [m] (:db/id m))
  (entity [m db] (entity (id m) (as-db db)))

  ;; nice for when working with the output of d/datoms and d/seek-datoms
  datomic.db.Datum
  (id [[e]] e)
  (entity [[e] db] (entity e (as-db db)))

  ;; nice for reaching schema or enum entities quickly,
  ;; however doesn't return a numeral for id like the rest do
  clojure.lang.Keyword
  (id [kw] kw)
  (entity [kw db] (let [db (as-db db)]
                    (entity (d/entid db kw) (as-db db))))

  ;; these two are when you have a naked entity (or schema entity) id
  java.lang.Long
  (id [id] id)
  (entity [id db] (d/entity (as-db db) id))
  java.lang.Integer
  (id [id] id)
  (entity [id db] (d/entity (as-db db) id))

  ;; lookup refs. doesn't work with id, because we don't get the db there.
  clojure.lang.PersistentVector
  (id [lookup]
    (assert (= 2 (count lookup))
            "A valid lookup ref has two elements.")
    (assert (keyword? (first lookup))
            "The first element of a valid lookup is a keyword.")
    lookup)
  (entity [lookup db] (d/entity (as-db db) lookup)))


;; =============================================================================
;; Entity
;; =============================================================================


(declare db? entity? entityd?)

(defn entities
  "Maps `entity` over `entids`, producing a vector of entities."
  [db & entids]
  (mapv
   (fn [entid]
     (entity entid db))
   entids))

(s/fdef entities
        :args (s/cat :db db?
                     :entids (s/spec (s/+ entity?)))
        :ret (s/+ entityd?))


;; =============================================================================
;; Predicates
;; =============================================================================


(defn db? [x]
  (satisfies? DatabaseReference x))


(defn entity? [x]
  (satisfies? EntityReference x))


(defn entityd? [x]
  (instance? datomic.query.EntityMap x))


;; =============================================================================
;; Queries
;; =============================================================================


(defn updated-at
  "Produce the instant in which `entity` was last updated."
  [db entity]
  (d/q '[:find (max ?tx-time) .
         :in $ ?e
         :where
         [?e _ _ ?tx _]
         [?tx :db/txInstant ?tx-time]]
       (d/history db) (:db/id entity)))

(s/fdef updated-at
        :args (s/cat :db db? :entity entity?)
        :ret inst?)

(defn created-at
  "Produce the instant in which `entity` was created."
  [db entity]
  (d/q '[:find (min ?tx-time) .
         :in $ ?e
         :where
         [?e _ _ ?tx _]
         [?tx :db/txInstant ?tx-time]]
       (d/history db) (:db/id entity)))

(s/fdef created-at
        :args (s/cat :db db? :entity entity?)
        :ret inst?)
