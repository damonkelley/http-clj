(ns http-clj.router-spec
  (:require [speclj.core :refer :all]
            [http-clj.router :as router]
            [http-clj.request-handler :as handler]))

(defn fallback [request])

(describe "a router"
  (with-stubs)
  (with routes [{:path "/a" :handlers {"GET" (stub :handler-a)}}
                {:path "/b" :handlers {"POST" (stub :handler-b)}}
                {:path #"^/pattern/.*$" :handlers {"GET" (stub :handler-z)}}])

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
  (with routes [{:path "/a" :handlers {"GET" :handler-get-a "POST" :handler-post-a}}
                {:path "/b" :handlers {"HEAD" :handler-head-b "GET" :handler-get-b}}])

  (it "chooses the get handler"
    (let [request {:method "GET" :path "/a"}]
      (should= :handler-get-a (router/choose-handler request @routes))))

  (it "chooses the post handler"
    (let [request {:method "POST" :path "/a"}]
      (should= :handler-post-a (router/choose-handler request @routes))))

  (it "chooses the head handler"
    (let [request {:method "HEAD" :path "/b"}]
      (should= :handler-head-b (router/choose-handler request @routes))))

  (it "returns method-not-allowed if the path exists but not the method"
    (let [request {:method "DELETE" :path "/a"}]
    (should= handler/method-not-allowed (router/choose-handler request @routes))))

  (it "returns not-found if the route does not exist"
    (let [request {:method "DELETE" :path "/c"}]
      (should= handler/not-found (router/choose-handler request @routes)))))
