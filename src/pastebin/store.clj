(ns pastebin.store)

(defn md5sum [s]
  (let [md5 (java.security.MessageDigest/getInstance "MD5")
        raw (.digest md5 (.getBytes s))]
    (format "%032x" (BigInteger. 1 raw))))

(defn insert [db k v]
  (do
    (swap! db assoc k v)
    {:key k}))

(defn add-item
  ([db k v]
   (insert db k v))
  ([db v]
   (insert db (md5sum v) v)))

(defn get-item [db id]
  (let [r (@db id)]
    {:paste r}))
