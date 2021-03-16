(ns zipper-manual.core
  (:require
   [hickory.core :as h]
   [hickory.zip :as hz]
   [clojure.edn :as edn]
   [clojure.zip :as zip]))

(def html (-> "https://grishaev.me/"
              java.net.URL.
              slurp))

(def doc-src (h/parse html))

(def doc-clj (h/as-hiccup doc-src))

(def doc-zip (hz/hiccup-zip doc-clj))


(defn iter-zip [zipper]
  (->> zipper
       (iterate zip/next)
       (take-while (complement zip/end?))))


(defn loc-h? [loc]
  (some-> loc zip/node first #{:h1 :h2 :h3 :h4 :h5 :h6}))


(defn loc-img? [loc]
  (some-> loc zip/node first (= :img)))

(defn loc->src [loc]
  (some-> loc zip/node second :src))

#_
(->> doc-zip
     iter-zip
     (filter loc-img?)
     (map loc->src))


(defn edn-save [data path]
  (spit path (pr-str data)))

(defn edn-load [path]
  (-> path slurp edn/read-string))


;; head

#_
(-> doc-zip
    zip/next
    zip/next
    zip/next
    (edn-save "zipper.edn")
    )


(-> "zipper.edn"
    edn-load
    (with-meta #:zip{:branch? clojure.core/sequential?
                     :children #'hickory.zip/children
                     :make-node #'hickory.zip/make})
    zip/node
    first

    )
