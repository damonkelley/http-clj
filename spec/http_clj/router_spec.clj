(ns http-clj.router-spec
  (:require [speclj.core :refer :all]
            [http-clj.router :as router]))

(describe "a router"
  (with-stubs)
  (with routes {["GET" "/a"] (stub :handler-a)
                ["POST" "/b"] (stub :handler-b)})
  (it "dispatches to handler-a"
    (router/route {:method "GET" :path "/a"} @routes)
    (should-not-have-invoked :handler-b)
    (should-have-invoked :handler-a {:times 1}))

  (it "dispatches to handler-b"
    (router/route {:method "POST" :path "/b"} @routes)
    (should-not-have-invoked :handler-a)
    (should-have-invoked :handler-b {:times 1}))

  (it "responds with not found there is no matching route"
    (let [{status :status body :body} (router/route
                                        {:method "GET" :path "/"} @routes)]
      (should= 404 status)))

  (it "will not find the handler if the path matches but the method does not"
    (let [{status :status body :body} (router/route
                                        {:method "GET" :path "/b"} @routes)]
      (should= 404 status))))
