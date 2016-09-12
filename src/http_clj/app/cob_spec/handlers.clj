(ns http-clj.app.cob-spec.handlers
  (:require [http-clj.request-handler :as handler]
            [http-clj.request-handler.filesystem :as filesystem]
            [http-clj.file :as file-helper]
            [http-clj.response :as response]
            [http-clj.response.cookies :as cookies]
            [clojure.core.match :refer [match]]
            [clojure.string :as string]))

(defn -static [request file]
  (let [method (:method request)
        directory? (.isDirectory file)
        exists? (.exists file)]

    (match [method directory? exists?]
      [(:or "GET" "HEAD") true _] (filesystem/directory request file)
      [(:or "GET" "HEAD") false true] (filesystem/file request (.getPath file))
      ["PATCH" false true] (filesystem/patch-file request (.getPath file))
      [_ _ _] (handler/not-found request))))

(defn static [request directory]
  (let [file (file-helper/resolve directory (:path request))]
    (-static request file)))

(defn log [request log]
    (response/create request (.toString log) :status 200))

(defn submit-form [request cache]
  (reset! cache (String. (:body request)))
  (response/create request ""))

(defn last-submission [request cache]
  (response/create request @cache))

(defn clear-submission [request cache]
  (reset! cache "")
  (response/create request ""))

(defn no-coffee [request]
  (response/create request "I'm a teapot" :status 418))

(defn tea [request]
  (response/create request ""))

(defn options [& allowed-options]
  (let [allow (string/join "," allowed-options)]
    #(response/create % "" :headers {"Allow" allow})))

(defn- present-query-params [query-params]
  (string/join "\n" (map #(string/join " = " %) query-params)))

(defn parameters [{:keys [query-params] :as request}]
  (response/create request (present-query-params query-params)))

(defn redirect-to-root [request]
  (let [host (get-in request [:headers :host])]
    (handler/redirect request (str "http://" host "/"))))

(defn- maybe-set-cookie-type [response cookie-type]
  (if cookie-type
    (cookies/set-cookie response "type" cookie-type)
    response))

(defn cookie [{:keys [query-params] :as request}]
  (let [cookie-type (get query-params "type")]
    (-> request
      (response/create "Eat")
      (maybe-set-cookie-type cookie-type))))

(defn- present-cookie [{headers :headers}]
  (if-let [-type (get-in headers [:cookie :type])]
    (str "mmmm " -type)
    ""))

(defn eat-cookie [request]
  (response/create request (present-cookie request)))
