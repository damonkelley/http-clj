# http-clj [![Build Status](https://travis-ci.org/damonkelley/http-clj.svg?branch=master)](https://travis-ci.org/damonkelley/http-clj)

An HTTP Server Library written in Clojure.


## Library Usage

For documentation on creating client applications, see [Creating Client Applications](doc/creating-client-applications.md)

## Testing

To run the specs

```shell
$ lein spec
```

To run the specs with the auto-runner

```shell
$ lein spec -a
```

## Cob Spec

The project comes with an example client application that satisfies [Cob Spec](http://github.com/8thlight/cob_spec). It is located at `http-clj.app.cob-spec`.

### Running Cob Spec against the server

1. Clone the Cob Spec project and start the Cob Spec server.

2. Start `http-clj` with the `cob-spec` profile and the path the Cob Spec public directory.

    ```bash
    $ lein with-profile cob-spec run -d <path to cob_spec/public>
    ```

3. Run the Cob Spec specs. There is no need to configure `SERVER_START_COMMAND` or `PUBLIC_DIR`.

### Running `http-clj.cob-spec`

With `lein run`

```bash
$ lein with-profile cob-spec run -d <directory> -p <port>
```

Via an Uberjar

```bash
$ lein with-profile cob-spec uberjar
$ java -jar target/http-clj-cob-spec.jar -d <directory> -p <port>
```

#### Command line options

##### `-p --port PORT`

Specifies the port to listen on. The default is 5000.


##### `-d --directory DIR`

Specifies the directory to serve files from. If absent, it will try to serve from `./public` in the current working directory.
