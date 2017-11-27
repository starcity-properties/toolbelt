(ns toolbelt.core
  #?(:cljs (:import [goog.ui IdGenerator])))


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


(defn assoc-some
  "Like assoc but only assocs when value is non-nil."
  [m & kvs]
  (assert (even? (count kvs)))
  (into (or m {})
        (for [[k v] (partition 2 kvs)
              :when (not (nil? v))]
          [k v])))


(defn assoc-when
  "Like assoc but only assocs when value is truthy."
  [m & kvs]
  (assert (even? (count kvs)))
  (into (or m {})
        (for [[k v] (partition 2 kvs)
              :when v]
          [k v])))


(defn conj-when
  "Like conj but ignores non-truthy values."
  ([coll x] (if x (conj coll x) coll))
  ([coll x & xs]
   (if xs
     (recur (conj-when coll x)
            (first xs)
            (next xs))
     (conj-when coll x))))


(defn dissoc-in
  "Dissociate this keyseq from m, removing any empty maps created as a result
   (including at the top-level)."
  [m [k & ks]]
  (when m
    (if-let [res (and ks (dissoc-in (get m k) ks))]
      (assoc m k res)
      (let [res (dissoc m k)]
        (when-not (empty? res)
          res)))))


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


(defn update-in-when
  "Like update-in but returns m unchanged if key-seq is not present."
  [m key-seq f & args]
  (let [found (get-in m key-seq ::missing)]
    (if-not (identical? ::missing found)
      (assoc-in m key-seq (apply f found args))
      m)))


(defn update-in-some
  "Like update-in but returns m unchanged if key-seq is not present."
  [m key-seq f & args]
  (let [found (get-in m key-seq)]
    (if-not (nil? found)
      (assoc-in m key-seq (apply f found args))
      m)))


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


(defn distinct-by
  "Returns elements of xs which return unique values according to f. If multiple
  elements of xs return the same value under f, the first is returned"
  [f xs]
  (let [s (atom #{})]
    (for [x     xs
          :let  [id (f x)]
          :when (not (contains? @s id))]
      (do (swap! s conj id)
          x))))


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

     (def log
       (.bind js/console.log js/console))

     (def warn
       (.bind js/console.warn js/console))

     (def error
       (.bind js/console.error js/console))))
