# Creating a client application

There are 3 main pieces to a `http-clj` client application.

1. Request Handlers
2. Routes
3. Configuring the server to use the application

#### Request Handlers (i.e. Controllers)

A request handler is simply a function that accepts a request map, and returns a response map.

A request map will look something like

```clojure
{:method "GET" :path "/" :version "HTTP/1.1" :params {"q" "foo"}
 :headers {:host "www.example.com"
           :user-agent "My user agent"}
 :body <byte array containing the body>}
```

So, a request handler is a function that takes this map and uses it to create and return a response map. A simple handler would be this `hello-world` handler.

```clojure
(require '[http-clj.response :as response])

(defn hello-world [request]
  (response/create
    request
    "Hello, world!"
    :status 200
    :headers {:content-type "text/plain"}))
```

`response/create` is function that will use a request map to create a response map. It accepts the request, a body, the status, and response headers.

Additionally, `http-clj` provides a number of predefined request handlers and request handler helpers. See `http-clj.request-handler`.

#### Routes

`http-clj.routes` provides mechanisms for building a router for your application. It provides a means to map a request handler to a path and HTTP method.

There are 5 route helpers

* `GET`
* `POST`
* `PUT`
* `PATCH`
* `DELETE`
* `OPTIONS`

Each helper takes a list of routes, a path, and a request handler.

Behind the scenes a router is simply a list of maps. Each route helper will add a handler for the corresponding HTTP method for the given path.

Below is an example routes table.

```clojure
(require '[http-clj.router :refer [GET POST]])

(def routes [request directory]
  (-> []
     (GET "/" index)
     (POST "/form" submit-form)))
```

The `route` function is called from the applications entrypoint.

```clojure
(require '[http-clj.router :refer [GET POST route]])

(def routes [request directory]
  (-> []
     (GET "/" index)
     (POST "/form" submit-form)))

(defn entrypoint [request]
  (route request routes))
```

`route` will take the request and the routes and use the path and method of the request to dispatch it to a handler.


#### Creating an application

An `http-clj` application is a map with two keys. `:entrypoint` and `:logger`.

`:entrypoint` is a request handler. Most likely this will be the function that wraps your `route` invocation.

`:logger` is anything that satisfies the `http-clj.logging/Logger` protocol.


```clojure
(def app {:entrypoint my-entrypoint
          :logger my-logger})
```


#### Running the application

`http-clj.server/run` is provided provided for use inside of a `-main` function. It accepts an application map, the port to run on, and a thread pool.

```clojure
(require '[http-clj.server :as server])

(def app {:entrypoint my-entrypoint
          :logger my-logger})

(defn -main [& args]
  (server/run app 5000 (Executors/newFixedThreadPool 50)))
```
