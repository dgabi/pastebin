(ns pastebin.filestore
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [pastebin.store :as store])
  (:import java.util.Base64))

(defn encode-base64 [to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(defn decode-base64 [to-decode]
  (String. (.decode (Base64/getDecoder) to-decode)))

(defn put-in-store [store item]
  (let [kv (str/split item #" ")]
    (store/add-item store (first kv) (decode-base64 (last kv)))))

(defn init-from-file [file-path store]
  (with-open [rdr (io/reader file-path)]
    (dorun (map #(put-in-store store %) (line-seq rdr)))))
