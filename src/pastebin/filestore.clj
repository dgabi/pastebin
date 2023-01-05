(ns pastebin.filestore
  (:require [clojure.string :as str]))

(defn put-in-store [store sline]
  (->> sline
       (map #(str/split % #" "))
       (#(map pastebin.store/add-item store (nth % 0) (nth % 1)))))

(defn init-from-file [file-path store]
  (with-open [rdr (clojure.java.io/reader file-path)]
    (->> (line-seq rdr)
         (#(put-in-store store %)))))
