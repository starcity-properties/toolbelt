(ns toolbelt.core
  #?(:cljs (:import [goog.ui IdGenerator]))
  (:require [plumbing.core :refer [dissoc-in]]))

(defn transform-when-key-exists
  "(transform-when-key-exists
     {:a 1
      :b 2}
     {:a #(inc %)
      :c #(inc %)})

   => {:a 2 :b 2}"
  [source transformations]
  (reduce
   (fn [m x]
     (merge m
            (let [[key value] x
                  t (get transformations key)]
              (if (and (map? value) (map? t))
                (assoc m key (transform-when-key-exists value t))
                (if-let [transform t]
                  {key (transform value)}
                  x)))))
   {}
   source))

(defn str->int
  "Converts a string to an integer. If the input is already a number,
  returns the input. Note, if a non-integer is passed, an error will
  result."
  ([str]
   (if (number? str)
     str
     (when-not (empty? str)
       (let [s (re-find #"\d+" str)]
         #?(:clj (Long. s)
            :cljs (js/parseInt s))))))
  ([m & keys]
   (reduce
    (fn [m k]
      (if-let [val (k m)]
        (assoc m k (str->int val))
        m))
    m keys)))

(defn dissoc-when
  "Dissoc from `korks' when the value is falsy, or when the optionally supplied
  predicate produces a falsy value when invoked on the value."
  ([m korks]
   (dissoc-when m korks identity))
  ([m korks pred]
   (let [korks (if (sequential? korks) korks [korks])]
     (if (pred (get-in m korks))
       (dissoc-in m korks)
       m))))

(defn find-by
  "Return the first element in `coll` matching `pred`; otherwise nil."
  [pred coll]
  (loop [x  (first coll)
         xs (rest coll)]
    (cond
      (pred x)    x
      (empty? xs) nil
      :otherwise  (recur (first xs) (rest xs)))))

(defn strip-namespaces
  "Remove all namespaces from keyword keys."
  [m]
  (reduce
   (fn [acc [k v]]
     (assoc acc (keyword (name k)) v))
   {}
   m))

(defn remove-at
  "Remove element at index `i` from vector `v`."
  [v i]
  (vec (concat (subvec v 0 i) (subvec v (inc i)))))

#?(:clj
   (do
     (defn round
       [x & [precision]]
       (if precision
         (let [scale (Math/pow 10 precision)]
           (-> x (* scale) Math/round (/ scale)))
         (Math/round x)))))

#?(:cljs
   (do
     (defn guid []
       (.getNextUniqueId (.getInstance IdGenerator)))

     (defn log [& args]
       (.apply js/console.log js/console (to-array args)))

     (defn warn [& args]
       (.apply js/console.warn js/console (to-array args)))

     (defn error [& args]
       (.apply js/console.error js/console (to-array args)))))
