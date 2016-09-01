(ns http-clj.router-spec
  (:require [speclj.core :refer :all]
            [http-clj.router :refer :all]
            [http-clj.router.route-helpers :refer [find-route]]
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
        (should= handler/not-found (choose-handler request @routes)))))

  (context "routes functions"
    (context "GET"
      (with routes [{:path "/a" :handlers {}}])
      (it "adds the GET handler route to the route"
        (let [route (-> @routes
                        (GET "/b" :get-handler)
                        (find-route "/b"))]
          (should= :get-handler (get-in route [:handlers "GET"]))))

      (it "appends the route to the end of the routes vector"
        (let [route (-> @routes
                        (GET "/b" :get-handler)
                        last)]
          (should= "/b" (:path route))))

      (it "provides a default HEAD handler"
        (let [route (-> @routes
                        (GET "/b" :handler)
                        (find-route "/b"))]
          (should-be clojure.test/function? (get-in route [:handlers "HEAD"]))))

      (it "an alternate HEAD handler can be specified"
        (let [route (-> @routes
                        (GET "/b" :get-handler :head-handler)
                        (find-route "/b"))]
          (should= :head-handler (get-in route [:handlers "HEAD"]))))

      (it "updates with a route if it is already defined"
        (let [route (-> @routes
                        (GET "/a" :handler)
                        (find-route "/a"))]
          (should= :handler (get-in route [:handlers "GET"]))))

      (it "can update a route with a pattern"
        (let [route (-> @routes
                        (GET #"^/.*$" :first-handler)
                        (GET #"^/.*$" :second-handler)
                        (find-route #"^/.*$")) ]
          (should= :second-handler (get-in route [:handlers "GET"])))))

    (context "POST"
      (with routes [{:path "/a" :handlers {}}])

      (it "appends a route if the path isn't defined"
        (let [routes (POST @routes "/b" :handler)]
          (should= {:path "/b" :handlers {"POST" :handler}} (last routes))))

      (it "updates the route if the path is defined"
        (let [routes (POST @routes "/a" :handler)]
          (should= {:path "/a" :handlers {"POST" :handler}} (first routes)))))

    (context "OPTIONS"
      (with routes [{:path "/path" :handlers {"POST" :post-handler}}])
      (it "adds the OPTIONS handler"
        (let [routes (OPTIONS @routes "/path" :options-handler)]
          (should= {"POST" :post-handler "OPTIONS" :options-handler}
                   (:handlers (find-route routes "/path"))))))))
