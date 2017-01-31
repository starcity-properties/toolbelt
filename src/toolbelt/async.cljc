(ns toolbelt.async
  (:require [clojure.core.async :as a :refer [<! go]]
            [toolbelt.predicates :refer [throwable?]]))

#?(:clj
   (do
     (defmacro <!!? [c]
       `(let [v# (a/<!! ~c)]
          (if (throwable? v#)
            (throw v#)
            v#)))))

(defmacro <!? [c]
  `(let [v# (<! ~c)]
     (if (throwable? v#)
       (throw v#)
       v#)))

(defmacro go-try
  "Wrap `body` in a `try-catch` block, causing just the Throwable be produced by
  the `go` block iff there is one."
  [& body]
  `(go (try ~@body
            (catch #?(:clj Throwable :cljs js/Error) ex#
                ex#))))
