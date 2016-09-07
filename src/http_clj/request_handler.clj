(ns http-clj.request-handler
  (:require [http-clj.file :as f]
            [http-clj.response :as response]
            [http-clj.presentation.template :as template]
            [http-clj.presentation.presenter :as presenter]
            [clojure.data.codec.base64 :as b64]
            [clojure.string :as string]))

(defn head [handler request]
  (-> request
      handler
      (assoc :body nil)))

(defn- matches? [parsed-credentials username password]
  (let [[parsed-username parsed-password] (string/split parsed-credentials #":")]
    (and (= parsed-username username) (= parsed-password password))))

(defn- authenticate [{headers :headers} username password]
  (if-let [authorization (:authorization headers)]
    (-> authorization
        (string/split #" ")
        second
        .getBytes
        b64/decode
        String.
        (matches? username password))
    false))

(defn auth [handler username password]
  (fn [request]
    (if (authenticate request username password)
      (handler request)
      (response/create request ""
                       :status 401
                       :headers {:www-authenticate "Basic realm=\"simple\""}))))

(defn not-found [request]
  (response/create request "Not Found" :status 404))

(defn method-not-allowed [request]
  (response/create request "Method Not Allowed" :status 405))

(defn redirect [request location]
  (response/create request "" :status 302 :headers {:location location}))
