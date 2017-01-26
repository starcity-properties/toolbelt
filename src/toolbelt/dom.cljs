(ns toolbelt.dom)

(defn by-class
  "Produce a Clojure sequence of DOM elements found under `class-name`."
  [class-name]
  (array-seq (.getElementsByClassName js/document class-name)))
