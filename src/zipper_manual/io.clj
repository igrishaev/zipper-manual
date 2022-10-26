(ns zipper-manual.io
  (:import java.io.File)
  (:require
   [clojure.zip :as zip]
   [clojure.java.io :as io]))


(defn file-zip [^String path]
  (zip/zipper
   (fn [^File f] (.isDirectory f))
   (fn [^File f] (seq (.listFiles f)))
   nil
   (new File path)))


(def fz
  (file-zip "/Users/ivan"))

(-> fz zip/node)
#object[java.io.File 0xe413375 "/Users/ivan"]

(-> fz zip/next zip/node)
#object[java.io.File 0x23e1b67 "/Users/ivan/.eclipse"]

(-> fz zip/next zip/next zip/next zip/node)
#object[java.io.File 0x138b3172 "/Users/ivan/.eclipse/org.eclipse.equinox.security/secure_storage"]

(def file-loc
  (-> fz zip/next zip/next zip/next))

(zip/path file-loc)

[#object[java.io.File 0xe413375 "/Users/ivan"]
 #object[java.io.File 0x2067c8ff "/Users/ivan/.eclipse"]
 #object[java.io.File 0x69304325 "/Users/ivan/.eclipse/org.eclipse.equinox.security"]]

(-> file-loc zip/up)


{:id 3
 :name "..."
 :description "..."
 :children [6 9 11 23]}

(defn entity-by-id [http-client entity-id]
  ...)


(defn entity-zip [http-client entity-id]
  (zip/zipper (fn [{:keys [children]}]
                (pos? (count children)))
              (fn [{:keys [children]}]
                (for [child children]
                  (entity-by-id http-client child)))
              nil
              (entity-by-id http-client entity-id)))
