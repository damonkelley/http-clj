# http-clj

An HTTP Server written in Clojure to satisfy [cob_spec](http://github.com/8thlight/cob_spec)

## Running the Server

With `lein run`

```bash
$ lein with-profile cob-spec run -d <directory> -p <port>
```

Via an Uberjar

```bash
$ lein with-profile cob-spec uberjar
$ java -jar target/http-clj-cob-spec.jar -d <directory> -p <port>
```

## Command line options



#### `-p --port PORT`

Specifies the port to listen on. The default is 5000.


#### `-d --directory DIR`

Specifies the directory to serve files from. If absent, it will try to serve from `./public` in the current working directory.
