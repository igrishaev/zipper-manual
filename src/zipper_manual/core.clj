(ns zipper-manual.core
  (:require
   [clojure.zip :as zip]
   [clojure.java.io :as io]
   [clojure.xml :as xml]

   [clojure.test :refer [deftest is]]))


#_
(:require
 [clojure.java.io :as io]
 [clojure.xml :as xml])

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
  (-> path
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


(defn loc-product? [loc]
  (-> loc zip/node :tag (= :product)))


(defn loc->product [loc]
  (-> loc zip/node :content first))


#_
(->> "products.xml"
     ->xml-zipper
     iter-zip
     (filter loc-product?)
     (map loc->product))


(def xml-data
  (-> "products.xml"
      io/resource
      io/file
      xml/parse))

(def orgs
  (:content xml-data))

(def products
  (mapcat :content orgs))

(def product-names
  (mapcat :content products))


(defn find-first [pred coll]
  (some (fn [x]
          (when (pred x)
            x))
        coll))

#_
(->> "products.xml"
     io/resource
     io/file
     xml/parse
     :content
     (mapcat :content)
     (mapcat :content))

#_
(->> "products-branch.xml"
     io/resource
     io/file
     xml/parse
     :content
     (mapcat :content)
     (mapcat :content))

#_
(->> "products-branch.xml"
     ->xml-zipper
     iter-zip
     (filter loc-product?)
     (map loc->product))


(defn node-product? [node]
  (some-> node :tag (= :product)))


#_
(->> "products-branch.xml"
     io/resource
     io/file
     xml/parse
     xml-seq
     (filter node-product?)
     (mapcat :content))


#_
(->> "products-branch.xml"
     io/resource
     io/file
     xml/parse
     xml-seq
     (map :tag)
     (remove nil?))

(defn loc-iphone? [loc]
  (let [node (zip/node loc)]
    (and (-> node :tag (= :product))
         (-> node :attrs :type (= "iphone")))))


(def loc-iphones
  (->> "products.xml"
       ->xml-zipper
       iter-zip
       (filter loc-iphone?)))


(def loc-orgs
  (->> loc-iphones
       (map zip/up)
       (map (comp :attrs zip/node))))


(defn loc-org? [loc]
  (-> loc zip/node :tag (= :organization)))


#_
(defn loc->org [loc]
  (->> loc
       (iterate zip/up)
       (drop-while (complement loc-org?))
       (first)))


(defn loc->org [loc]
  (->> loc
       (iterate zip/up)
       (find-first loc-org?)))


(->> "products-branch.xml"
     ->xml-zipper
     iter-zip
     (filter loc-iphone?)
     (map loc->org)
     (map (comp :attrs zip/node))
     (set))


(defn loc-fiber? [loc]
  (some-> loc zip/node :attrs :type (= "fiber")))


(->> "products-bundle.xml"
     ->xml-zipper
     iter-zip
     (filter loc-fiber?)
     (map (comp first :content zip/node)))


(defn loc-in-bundle? [loc]
  (some-> loc zip/up zip/node :tag (= :bundle)))


(->> "products-bundle.xml"
     ->xml-zipper
     iter-zip
     (filter loc-fiber?)
     (remove loc-in-bundle?)
     (map loc->org)
     (map (comp :attrs zip/node))
     (set))


(defn loc-lefts [loc]
  (->> loc
       (iterate zip/left)
       (take-while some?)
       (rest)))

(defn loc-rights [loc]
  (->> loc
       (iterate zip/right)
       (take-while some?)
       (rest)))


(defn loc-neighbors [loc]
  (concat (loc-lefts loc)
          (loc-rights loc)))


(defn node-neighbors [loc]
  (concat (zip/lefts loc)
          (zip/rights loc)))


(defn node-fiber? [node]
  (some-> node :attrs :type (= "fiber")))


#_
(defn with-fiber? [loc]
  (let [nodes (node-neighbors loc)]
    (some node-fiber? nodes)))

(defn with-fiber? [loc]
  (let [nodes (node-neighbors loc)]
    (find-first node-fiber? nodes)))


(->> "products-bundle.xml"
     ->xml-zipper
     iter-zip
     (filter loc-iphone?)
     (filter loc-in-bundle?)
     (filter with-fiber?)
     (map loc->org)
     (map (comp :attrs zip/node))
     (set))

#_
(-> [1 2 3]
    zip/vector-zip
    zip/down
    zip/right
    node-neighbors)



(def data [1 2 [3 4 [5 :error]]])

(defn loc-error? [loc]
  (some-> loc zip/node (= :error)))

(def loc-error
  (->> data
       zip/vector-zip
       iter-zip
       (some (fn [loc]
               (when (loc-error? loc)
                 loc)))))

(-> loc-error
    (zip/replace :ok)
    zip/root)
;; [1 2 [3 4 [5 :ok]]]


;; ---------

(def data [1 2 [5 nil 2 [3 nil]] nil 1])

#_
(loop [loc (zip/vector-zip data)]
  (if (zip/end? loc)
    (zip/node loc)
    (if (-> loc zip/node nil?)
      (recur (zip/next (zip/replace loc 0)))
      (recur (zip/next loc)))))
;; [1 2 [5 0 2 [3 0]] 0 1]

;; -------------


(def data [-1 2 [5 -2 2 [-3 2]] -1 5])

(defn abs [num]
  (if (neg? num)
    (- num)
    num))

(loop [loc (zip/vector-zip data)]
  (if (zip/end? loc)
    (zip/node loc)
    (if (and (-> loc zip/node number?)
             (-> loc zip/node neg?))
      (recur (zip/next (zip/edit loc abs)))
      (recur (zip/next loc)))))

;; [1 2 [5 2 2 [3 2]] 1 5]

;; --------------


(defn find-loc [loc loc-pred]
  (->> loc
       iter-zip
       (some (fn [loc]
               (when (loc-pred loc)
                 loc)))))


(defn alter-loc [loc loc-fn]
  (loop [loc loc]
    (if (zip/end? loc)
      loc
      (recur (-> loc loc-fn zip/next)))))


;; ----

(defn loc-abs [loc]
  (if (and (-> loc zip/node number?)
           (-> loc zip/node neg?))
    (zip/edit loc abs)
    loc))


#_
(-> [-1 2 [5 -2 2 [-3 2]] -1 5]
    zip/vector-zip
    (alter-loc loc-abs)
    zip/node)

;; [1 2 [5 2 2 [3 2]] 1 5]

;; -----------

(def data [1 2 [3 4 [5 :error]]])

(defn loc-error? [loc]
  (some-> loc zip/node (= :error)))

(def loc-error
  (-> data
      zip/vector-zip
      (find-loc loc-error?)))

(-> loc-error
    (zip/replace :ok)
    zip/root)
;; [1 2 [3 4 [5 :ok]]]


;; alter iphone price


(defn alter-attr-price [node ratio]
  (update-in node [:attrs :price]
             (fn [price]
               (->> price
                    read-string
                    (* ratio)
                    (format "%.2f")))))


(defn alter-iphone-price [loc]
  (if (loc-iphone? loc)
    (zip/edit loc alter-attr-price 0.9)
    loc))



#_
(-> "products-price.xml"
    ->xml-zipper
    (alter-loc alter-iphone-price)
    zip/node
    xml/emit)

"
<?xml version='1.0' encoding='UTF-8'?>
<catalog>
<organization name='re-Store'>
<product price='8.99' type='fiber'>
VIP Fiber Plus
</product>
<product price='809.99' type='iphone'>
iPhone 11 Pro
</product>
</organization>
<organization name='DNS'>
<branch name='Office 2'>
<bundle>
<product price='9.99' type='fiber'>
Premium iFiber
</product>
<product price='899.99' type='iphone'>
iPhone 11 Pro
</product>
</bundle>
</branch>
</organization>
</catalog>
"


;; add to bundle


(defn loc-bundle? [loc]
  (some-> loc zip/node :tag (= :bundle)))

(def node-headset
  {:tag :product
   :attrs {:type "headset"
           :price "199.99"}
   :content ["AirPods Pro"]})

(defn add-to-bundle [loc]
  (if (loc-bundle? loc)
    (zip/append-child loc node-headset)
    loc))

(-> "products-price.xml"
    ->xml-zipper
    (alter-loc add-to-bundle)
    zip/node
    xml/emit)

"
<?xml version='1.0' encoding='UTF-8'?>
<catalog>
<organization name='re-Store'>
<product price='8.99' type='fiber'>
VIP Fiber Plus
</product>
<product price='899.99' type='iphone'>
iPhone 11 Pro
</product>
</organization>
<organization name='DNS'>
<branch name='Office 2'>
<bundle>
<product price='9.99' type='fiber'>
Premium iFiber
</product>
<product price='999.99' type='iphone'>
iPhone 11 Pro
</product>
<product type='headset' price='199.99'>
AirPods Pro
</product>
</bundle>
</branch>
</organization>
</catalog>
"

(defn append-childs [loc items]
  (reduce (fn [loc item]
            (zip/append-child loc item))
          loc
          items))

(defn disband-bundle [loc]
  (if (loc-bundle? loc)
    (let [products (zip/children loc)
          loc-prev (zip/remove loc)]
      (append-childs loc-prev products))
    loc))

(-> "products-price.xml"
    ->xml-zipper
    (alter-loc disband-bundle)
    zip/node
    xml/emit)


"
<?xml version='1.0' encoding='UTF-8'?>
<catalog>
<organization name='re-Store'>
<product price='8.99' type='fiber'>
VIP Fiber Plus
</product>
<product price='899.99' type='iphone'>
iPhone 11 Pro
</product>
</organization>
<organization name='DNS'>
<branch name='Office 2'>
<product price='9.99' type='fiber'>
Premium iFiber
</product>
<product price='999.99' type='iphone'>
iPhone 11 Pro
</product>
</branch>
</organization>
</catalog>
"


{:tag :organization
 :attrs {:name "DNS"}}


{:tag :product
 :attrs {:type "iphone"}
 :content ["iPhone 11 Pro"]}

{:tag :product
 :attrs {:type "fiber"}
 :content ["Premium iFiber"]}


{:tag :organization
 :attrs {:name "DNS"}
 :content [{:tag :product
            :attrs {:type "iphone"}
            :content ["iPhone 11 Pro"]}
           {:tag :product
            :attrs {:type "fiber"}
            :content ["Premium iFiber"]}]}


(-> [1 2 3]
    zip/vector-zip
    zip/down
    zip/right)

#_
[2 {:l [1], :pnodes [[1 2 3]], :ppath nil, :r (3)}]


(def loc-2
  (-> [1 2 3]
      zip/vector-zip
      zip/down
      zip/right
      (zip/edit * 2)))

#_
[4 {:l [1], :pnodes [[1 2 3]], :ppath nil, :r (3), :changed? true}]

(-> loc-2
    zip/up
    zip/node)


(-> [1 2 3]
    zip/vector-zip
    zip/down
    zip/right
    (zip/edit * 2)
    zip/root)




(def zip-rand
  (zip/zipper (constantly true)
              (constantly (seq [1 2 3]))
              nil
              0))


(-> zip-rand zip/down zip/down zip/right zip/node)


(def zip-123
  (zip/zipper any?
              (constantly (seq [1 2 3]))
              nil
              1))



(def loc-2
  (-> zip-123
      zip/down
      zip/right))

(zip/node loc-2)
;; 2


(def down-right (comp zip/right zip/down))

(-> loc-2
    down-right
    down-right
    down-right
    down-right
    down-right
    zip/node)
;; 2



;; rules
[[:usd :rub] [:rub :eur] [:eur :lir]]

:usd ;; from
:rub ;; to

[:usd :rub :eur]

(defn exchanges [rules from to]
  )


(def rules
  [[:usd :rub]
   [:usd :lir]
   [:rub :eur]
   [:rub :yen]
   [:eur :lir]
   [:lir :tug]])

(def from :usd)
(def to :yen)

(def usd-children
  (for [[v1 v2] rules
        :when (= v1 from)]
    v2))
;; (:rub :lir)

(defn get-children [value]
  (for [[v1 v2] rules
        :when (= v1 value)]
    v2))


(def zip-val
  (zip/zipper keyword?
              get-children
              nil
              from))


(def loc-to
  (->> zip-val
       iter-zip
       (some (fn [loc]
               (when (-> loc zip/node (= to))
                 loc)))))


(def rules
  [[:usd :rub]
   [:usd :lir]
   [:rub :eur]
   [:lir :yen]
   [:rub :yen]
   [:eur :lir]
   [:lir :tug]])

(def from :usd)
(def to :yen)


(def locs-to
  (->> zip-val
       iter-zip
       (filter (fn [loc]
                 (-> loc zip/node (= to))))))


(for [loc locs-to]
  (conj (zip/path loc) (zip/node loc)))



(conj (zip/path loc-to) (zip/node loc-to))
;; [:usd :rub :yen]


(def val-chains
  '([:usd :rub :eur :lir :yen]
    [:usd :rub :yen]
    [:usd :lir :yen]))


(defn get-shortest-chains
  [chains]
  (when (seq chains)
    (let [count->chains (group-by count chains)
          min-count (apply min (keys count->chains))]
      (get count->chains min-count))))


(defn exchanges [rules from to]

  (let [get-children
        (fn [value]
          (for [[v1 v2] rules
                :when (= v1 value)]
            v2))

        zipper (zip/zipper keyword?
                           get-children
                           nil
                           from)

        locs-to
        (->> zipper
             iter-zip
             (filter (fn [loc]
                       (when (-> loc zip/node (= to))
                         loc))))]

    (get-shortest-chains
     (for [loc locs-to]
       (conj (zip/path loc) (zip/node loc))))))


#_
(exchanges
 [[:usd :rub]
  [:usd :lir]
  [:rub :eur]
  [:lir :yen]
  [:rub :yen]
  [:eur :lir]
  [:lir :tug]]
 :usd
 :yen)


(deftest test-simple
  (is (= [[:usd :rub]]
         (exchanges [[:usd :rub]] :usd :rub))))


(deftest test-reverse-err
  (is (nil? (exchanges [[:rub :usd]] :usd :rub))))


(deftest test-no-solution
  (is (nil? (exchanges [[:rub :usd] [:lir :eur]] :usd :eur))))


(deftest test-two-ways
  (is (= [[:usd :eur :rub]
          [:usd :lir :rub]]
         (exchanges [[:usd :eur]
                     [:eur :rub]
                     [:usd :lir]
                     [:lir :rub]] :usd :rub))))


(deftest test-short-ways-only
  (is (= [[:usd :eur :rub]
          [:usd :lir :rub]]
         (exchanges [[:usd :eur]
                     [:eur :rub]
                     [:usd :lir]
                     [:lir :rub]
                     [:usd :yen]
                     [:yen :eur]] :usd :rub))))


(defn loc-children [loc]
  (when-let [loc-child (zip/down loc)]
    (->> loc-child
         (iterate zip/right)
         (take-while some?))))


(defn loc-layers [loc]
  (->> [loc]
       (iterate (fn [locs]
                  (mapcat loc-children locs)))
       (take-while seq)))


(defn loc-seq-layers [loc]
  (apply concat (loc-layers loc)))



(def rules2
  [[:rub :usd]
   [:usd :eur]
   [:eur :rub]

   [:rub :lir]
   [:lir :eur]
   [:eur :din]
   [:din :tug]])


(defn exchange2 [rules from to]

  (letfn [(get-children [value]
            (seq (for [[v1 v2] rules
                       :when (= v1 value)]
                   v2)))

          (loc-to? [loc]
            (-> loc zip/node (= to)))

          (find-locs-to [layer]
            (seq (filter loc-to? layer)))

          (->exchange [loc]
            (conj (zip/path loc) (zip/node loc)))]

    (let [zipper (zip/zipper keyword?
                             get-children
                             nil
                             from)]

      (->> zipper
           loc-layers
           (take 5)
           (some find-locs-to)
           (map ->exchange)))))



(->> [1 [2 [3] 4] 5]
     zip/vector-zip
     iter-zip
     (map zip/node)
     (map println))

;; 1
;; [2 [3] 4]
;; 2
;; [3]
;; 3
;; 4
;; 5


#_
(def zip-123
  (zip/zipper any?
              (constantly (seq [1 2 3]))
              nil
              1))


;; (->> zip-123 iter-zip (take 10) (map zip/node))


#_
(def rules
  [[:rub :usd]
   [:usd :eur]
   [:eur :rub]])


(-> [1 2 3]
    zip/vector-zip
    zip/children)

;; (1 2 3)


(-> [1 2 3]
    zip/vector-zip
    loc-children)

#_
([1 {:l [] :pnodes [[1 2 3]] :ppath nil :r (2 3)}]
 [2 {:l [1] :pnodes [[1 2 3]] :ppath nil :r (3)}]
 [3 {:l [1 2] :pnodes [[1 2 3]] :ppath nil :r nil}])


(let [layers (-> [[[[1]]] 2 [[[3]]] 3]
                 zip/vector-zip
                 loc-layers)]
  (for [layer layers]
    (->> layer
         (map zip/node)
         println)))


;; ([[[[1]]] 2 [[[3]]] 3])
;; ([[[1]]] 2 [[[3]]] 3)
;; ([[1]] [[3]])
;; ([1] [3])
;; (1 3)


#_
(->> zipper
     loc-layers
     (take 5)
     (some find-locs-to)
     (map ->exchange))

(def to :foo)

(defn loc-to? [loc]
  (-> loc zip/node (= to)))


(def loc-to
  (->> zip-val
       iter-zip
       (find-first loc-to?)))


(def locs-to
  (->> zip-val
       iter-zip
       (filter loc-to?)))


(def loc-error
  (->> data
       zip/vector-zip
       iter-zip
       (find-first loc-error?)))


(defn find-loc [loc loc-pred]
  (->> loc
       iter-zip
       (find-first loc-pred)))


(defn alter-loc [loc loc-fn]
  (loop [loc loc]
    (if (zip/end? loc)
      loc
      (-> loc loc-fn zip/next recur))))


(defn loc-2? [loc]
  (-> loc zip/node (= 2)))
