(ns zipper-manual.core
  (:require
   [clojure.zip :as zip]
   [clojure.java.io :as io]
   [clojure.xml :as xml]))

#_
(->> [1 2 3]
     zip/vector-zip
     zip/down
     zip/right
     zip/node)
2

#_
(->> [1 2 3]
     zip/vector-zip
     zip/down
     zip/right)

#_
[2 {:l [1], :pnodes [[1 2 3]], :ppath nil, :r (3)}]


#_
(defn vector-zip
  [root]
  (zipper vector?
          seq
          ...
          root))


#_

(->> [1 2 3]
     zip/vector-zip
     zip/down
     zip/right
     zip/right
     zip/left
     zip/node)


#_
(->> [1 2 3]
     zip/vector-zip
     zip/down
     zip/left)
nil


#_
(-> [1 2 3]
    zip/vector-zip
    zip/down
    zip/left
    zip/left
    zip/left
    zip/left)

nil


#_
(-> [1 2 3]
    zip/vector-zip
    zip/down
    zip/left
    zip/down)
;; Execution error (NullPointerException) at zipper-manual.core/eval6031 (form-init14080226076261358478.clj:123).
;; null


(def loc3
  (-> [1 [2 3] 4]
      zip/vector-zip
      zip/down
      zip/right
      zip/down
      zip/right))

(zip/node loc3)
3


#_
(-> "test"
    zip/vector-zip
    zip/down)
nil


(def loc2
  (-> [1 2 3]
      zip/vector-zip
      zip/down
      zip/right))

(-> loc2 zip/node)

(-> loc2 zip/right zip/node)

(-> loc2 zip/left zip/node)


(get-in [1 [2 3] 4] [1 1])
3

(-> {:users [{:name "Ivan"}]}
    :users
    first
    :name)
"Ivan"


(def vz (zip/vector-zip [1 [2 3] 4]))

(-> vz zip/node)
(-> vz zip/next zip/node)
(-> vz zip/next zip/next zip/node)
(-> vz zip/next zip/next zip/next zip/node)

#_
(-> vz zip/next zip/next zip/next zip/next zip/next zip/next zip/next zip/next zip/next zip/next zip/next zip/next zip/next zip/next zip/node zip/node)


(defn iter-zip [zipper]
  (->> zipper
       (iterate zip/next)
       (take-while (complement zip/end?))))


#_
(iterate zip/next vz)


#_
(def loc-seq (iterate zip/next vz))

#_
(->> loc-seq
     (take 6)
     (map zip/node))


#_
(->> [1 [2 3] 4]
     zip/vector-zip
     iter-zip
     (map zip/node))


(defn loc-error? [loc]
  (-> loc zip/node (= :error)))


#_
(->> [1 [2 3 [:test [:foo :error]]] 4]
     zip/vector-zip
     iter-zip
     (some loc-error?))


#_
(def loc-end
  (-> [1 2 3]
      zip/vector-zip
      zip/next
      zip/next
      zip/next
      zip/next))


(def map-data
  {:foo 1
   :bar 2
   :baz {:test "hello"
         :word {:nested true}}})


(def entry? (partial instance? clojure.lang.MapEntry))


(defn map-zip [mapping]
  (zip/zipper
   (some-fn entry? map?)
   (fn [x]
     (cond
       (map? x) (seq x)
       (and (entry? x)
            (-> x val map?))
       (-> x val seq)))
   nil
   mapping))

#_
(->> {:foo 42 :bar {:baz 11
                    :user/name "Ivan"}}
     map-zip
     iter-zip
     (map zip/node))

#_
(... [:foo 42] [:bar {:baz 11, :user/name "Ivan"}] [:baz 11] [:user/name "Ivan"])


(defn loc-err-auth? [loc]
  (-> loc zip/node (= [:error :auth])))


(->> {:response {:error :expired
                 :auth :failed}}
     map-zip
     iter-zip
     (some loc-err-auth?))


(->> {:response {:error :auth}}
     map-zip
     iter-zip
     (some loc-err-auth?))


#_
(->> "products.xml" io/resource io/file xml/parse xml-seq second :tag)

(defn ->xml-zipper [path]
  (->> path
       io/resource
       io/file
       xml/parse
       zip/xml-zip))


#_
(->> "products.xml"
     ->xml-zipper
     iter-zip
     (map (fn [loc]
            (-> loc zip/node :tag))))
