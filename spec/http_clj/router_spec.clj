(ns http-clj.router-spec
  (:require [speclj.core :refer :all]
            [http-clj.router :as router]
            [http-clj.request-handler :as handler]))

(defn fallback [request])

(describe "a router"
  (with-stubs)
  (with routes [["GET" "/a" (stub :handler-a)]
                ["POST" "/b" (stub :handler-b)]
                ["GET" #"^/pattern/.*$" (stub :handler-z)]])
  (it "dispatches to handler-a"
    (router/route {:method "GET" :path "/a"} @routes)
    (should-not-have-invoked :handler-b)
    (should-have-invoked :handler-a {:times 1}))

  (it "dispatches to handler-b"
    (router/route {:method "POST" :path "/b"} @routes)
    (should-not-have-invoked :handler-a)
    (should-have-invoked :handler-b {:times 1}))

  (it "responds with not found if there is no matching route"
    (let [{status :status body :body} (router/route
                                        {:method "GET" :path "/"} @routes)]
      (should= 404 status)))

  (it "responds with method not allowed if the path matches but the method does not"
    (let [{status :status body :body} (router/route
                                        {:method "GET" :path "/b"} @routes)]
      (should= 405 status)))

  (it "it will find the handler for a route defined as a pattern"
    (let [request {:method "GET" :path "/pattern/handler-z"}
          {status :status body :body} (router/route request @routes)]
      (should-have-invoked :handler-z {:times 1}))))

(describe "choose-handler"
  (with routes [["GET" "/a" :handler-a]
                ["POST" "/a" :handler-post-a]])
  (it "finds the handler that matches the method and path"
    (let [request {:method "GET" :path "/a"}]
    (should= :handler-a (router/choose-handler request @routes))))

  (it "returns method-not-allowed if the path exists but not the method"
    (let [request {:method "DELETE" :path "/a"}]
    (should= handler/method-not-allowed (router/choose-handler request @routes))))

  (it "returns not-found if neither the path or method match"
    (let [request {:method "DELETE" :path "/b"}]
    (should= handler/not-found (router/choose-handler request @routes)))))

(describe "path-matches?"
  (with request-path "/path/to/resource")
  (it "matches a string path"
    (should= true (router/path-matches? @request-path "/path/to/resource"))
    (should= false (router/path-matches? @request-path "/resource")))

  (it "matches a regex path"
    (should= false (router/path-matches? @request-path #"/foo/.*"))
    (should= true (router/path-matches? @request-path #"^/path/.*/resource$"))))
