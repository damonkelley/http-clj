(ns http-clj.router.route-helpers-spec
  (:require [speclj.core :refer :all]
            [http-clj.router.route-helpers :refer :all]))

(describe "router.route-helpers"
  (context "find-route"
    (with routes [{:path "/a" :handlers {"GET" :handler-a }}
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
    (with routes [{:path "/a" :handlers {"GET" :get-handler "POST" :post-handler}}
                  {:path #"^/path/.*$" :handlers {}}])

    (it "update the route if it exists"
      (let [routes (update-route @routes {:path "/a" :handlers {"GET" :new-handler}})]
        (should= {:path "/a" :handlers {"GET" :new-handler "POST" :post-handler}}
                 (find-route routes "/a"))))

    (it "appends a new route if the path doesn't exist"
      (let [routes (update-route @routes {:path "/b" :handlers {}})]
        (should= {:path "/b" :handlers {}} (last routes))))

    (it "can update a route with a pattern path"
      (let [routes (update-route @routes {:path #"^/path/.*$" :handlers {"GET" :handler}})]
        (should= {"GET" :handler}  (:handlers (find-route routes #"^/path/.*$")))))))
