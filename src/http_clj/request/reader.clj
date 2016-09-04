(ns http-clj.request.reader
  (:require [http-clj.connection :as connection]))

(defn- continue-reading? [-byte]
  (some #(= -byte %) (map int [\newline -1])))

(defn- remove-carriage-returns [-bytes]
  (remove #(= (int \return) %) -bytes))

(defn- convert-to-string [-bytes]
  (-> -bytes
      remove-carriage-returns
      byte-array
      String.))

(defn readline [{conn :conn}]
    (loop [-bytes []]
      (let [-byte (connection/read-byte conn)]
        (if (continue-reading? -byte)
          (convert-to-string -bytes)
          (recur (conj -bytes -byte))))))

(defn read-headers
  ([request] (read-headers request []))
  ([request headers]
   (let [header (readline request)]
     (if (empty? header)
       headers
       (recur request (conj headers header))))))

(defn read-body [{:keys [headers conn]}]
  (if-let [content-length (get headers :content-length)]
    (connection/read-bytes conn content-length)))
