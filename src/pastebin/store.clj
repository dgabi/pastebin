(ns pastebin.store
  (:require [clojure.core.async
             :as a
             :refer [>! <! >!! <!! go chan buffer close! thread
                     alts! alts!! timeout]])
  (:import java.util.Base64))

(defn encode-base64 [to-encode]
  (.encodeToString (Base64/getEncoder) (.getBytes to-encode)))

(defn md5sum [s]
  (let [md5 (java.security.MessageDigest/getInstance "MD5")
        raw (.digest md5 (.getBytes s))]
    (format "%032x" (BigInteger. 1 raw))))

(def save-chan (chan))

(go println (<! save-chan))

(defn insert [db k v]
  (do
    (swap! db assoc k v)
    (>!! save-chan (str k " " (encode-base64 v)))
    {:key k}))

(defn add-item
  ([db k v]
   (insert db k v))
  ([db v]
   (insert db (md5sum v) v)))

(defn get-item [db id]
  (let [r (@db id)]
    {:paste r}))
