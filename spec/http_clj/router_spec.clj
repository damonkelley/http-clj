(ns http-clj.router-spec
  (:require [speclj.core :refer :all]
            [http-clj.router :refer :all]
            [http-clj.request-handler :as handler]))

(describe "router"
  (context "route"
    (with-stubs)
    (with routes [{:path "/a" :handlers {"GET" (stub :handler-a)}}
                  {:path "/b" :handlers {"POST" (stub :handler-b)}}
                  {:path #"^/pattern/.*$" :handlers {"GET" (stub :handler-z)}}])

    (it "dispatches to handler-a"
      (route {:method "GET" :path "/a"} @routes)
      (should-not-have-invoked :handler-b)
      (should-have-invoked :handler-a {:times 1}))

    (it "dispatches to handler-b"
      (route {:method "POST" :path "/b"} @routes)
      (should-not-have-invoked :handler-a)
      (should-have-invoked :handler-b {:times 1}))

    (it "responds with not found if there is no matching route"
      (let [{status :status body :body} (route
                                          {:method "GET" :path "/"} @routes)]
        (should= 404 status)))

    (it "responds with method not allowed if the path matches but the method does not"
      (let [{status :status body :body} (route
                                          {:method "GET" :path "/b"} @routes)]
        (should= 405 status)))

    (it "it will find the handler for a route defined as a pattern"
      (let [request {:method "GET" :path "/pattern/handler-z"}
            {status :status body :body} (route request @routes)]
        (should-have-invoked :handler-z {:times 1}))))

  (context "find-route"
    (with routes [{:path "/a" :handlers {"GET" :handler-a}}
                  {:path "/b" :handlers {"POST" :handler-b}}
                  {:path #"^/pattern/.*$" :handlers {"GET" :handler-z}}])

    (it "finds the route by path"
      (should= {:path "/a" :handlers {"GET" :handler-a}}
               (find-route @routes "/a")))

    (it "is nil if there is no route for the path"
      (should= nil (find-route @routes "/c")))

    (it "can find a route using a pattern"
      (should= {"GET" :handler-z} (:handlers (find-route @routes #"^/pattern/.*$")))))

  (context "update-route"
    (with routes [{:path "/a" :handlers {"GET" :handler-a}}
                  {:path #"^/path/.*$" :handlers {}}])

    (it "update the route if it exists"
      (let [routes (update-route @routes {:path "/a" :handlers {"GET" :new-handler}})]
        (should= {:path "/a" :handlers {"GET" :new-handler}} (find-route routes "/a"))))

    (it "appends a new route if the path doesn't exist"
      (let [routes (update-route @routes {:path "/b" :handlers {}})]
        (should= {:path "/b" :handlers {}} (last routes))))

    (it "can update a route with a pattern path"
      (let [routes (update-route @routes {:path #"^/path/.*$" :handlers {"GET" :handler}})]
        (should= {"GET" :handler}  (:handlers (find-route routes #"^/path/.*$"))))))

  (context "choose-handler"
    (with routes [{:path "/a" :handlers {"GET" :handler-get-a "POST" :handler-post-a}}
                  {:path "/b" :handlers {"HEAD" :handler-head-b "GET" :handler-get-b}}])

    (it "chooses the get handler"
      (let [request {:method "GET" :path "/a"}]
        (should= :handler-get-a (choose-handler request @routes))))

    (it "chooses the post handler"
      (let [request {:method "POST" :path "/a"}]
        (should= :handler-post-a (choose-handler request @routes))))

    (it "chooses the head handler"
      (let [request {:method "HEAD" :path "/b"}]
        (should= :handler-head-b (choose-handler request @routes))))

    (it "returns method-not-allowed if the path exists but not the method"
      (let [request {:method "DELETE" :path "/a"}]
        (should= handler/method-not-allowed (choose-handler request @routes))))

    (it "returns not-found if the route does not exist"
      (let [request {:method "DELETE" :path "/c"}]
        (should= handler/not-found (choose-handler request @routes))))))

(describe "route helpers"
  (context "GET"
    (with routes [{:path "/a" :handlers {}}])
    (it "adds the GET handler route to the route"
      (let [routes (GET @routes "/b" :get-handler)]
        (should= :get-handler (get-in (last routes) [:handlers "GET"]))))

    (it "appends the route to the end of the routes vector"
      (should= "/b" (:path (last (GET @routes "/b" :get-handler)))))

    (it "provides a default HEAD handler"
      (let [routes (GET @routes "/b" :handler)]
        (should-be clojure.test/function? (get-in (last routes) [:handlers "HEAD"]))))

    (it "an alternate HEAD handler can be specified"
      (let [routes (GET @routes "/b" :get-handler :head-handler)]
        (should= :head-handler (get-in (last routes) [:handlers "HEAD"]))))

    (it "updates with a route if it is already defined"
      (let [route (find-route (GET @routes "/a" :handler) "/a")]
        (should= :handler (get-in route [:handlers "GET"]))))

    (it "can update a route with a pattern"
      (let [routes (GET @routes #"^/.*$" :first-handler)
            routes (GET routes #"^/.*$" :second-handler)]
        (should= :second-handler (get-in (find-route routes #"^/.*$") [:handlers "GET"]))))))
