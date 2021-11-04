(ns pastebin.store)

(defn md5sum [s]
  (let [md5 (java.security.MessageDigest/getInstance "MD5")
        raw (.digest md5 (.getBytes s))]
    (format "%032x" (BigInteger. 1 raw))))

(defn add-item [db data]
  (let [hash-value (md5sum data)]
    (do
      (swap! db assoc hash-value data)
      {:key hash-value})))

(defn get-item [db id]
  (let [r (@db id)]
    {:paste r}))

