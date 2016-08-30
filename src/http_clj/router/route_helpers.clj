(ns http-clj.router.route-helpers
  (:import java.util.regex.Pattern))

(defmulti path-matches?
  (fn [path route-path]
    [(type path) (type route-path)]))

(defmethod path-matches? [String String]
  [path route-path]
  (= path route-path))

(defmethod path-matches? [String Pattern]
  [path route-path]
  (not (nil? (re-matches route-path path))))

(defmethod path-matches? [Pattern Pattern]
  [path route-path]
  (= (.pattern path) (.pattern route-path)))

(defmethod path-matches? [Pattern String]
  [_ _]
  ; A pattern should not match to a string in this direction
  ; For instance, #"/.*" will match against the first path it
  ; encounters instead of matching against a route with the
  ; #"/.*" path.
  false)

(defn find-route [routes path]
  (first (filter #(path-matches? path (:path %)) routes)))

(defn- associate-route-ids [routes]
  (map-indexed vector routes))

(defn- filter-id-route-pairs-by-path [id-route-pairs path]
  (filter #(path-matches? path (:path (second %))) id-route-pairs))

(defn- extract-first-id [id-route-pairs]
  (first (first id-route-pairs)))

(defn- lookup-route-id [routes path]
  (-> routes
       associate-route-ids
       (filter-id-route-pairs-by-path path)
       extract-first-id))

(defn update-route [routes route]
  (if-let [route-id (lookup-route-id routes (:path route))]
    (update routes route-id merge route)
    (conj routes route)))
