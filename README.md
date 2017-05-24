# Thermostat Web Gateway

This is a HTTP API server for [Thermostat](http://icedtea.classpath.org/thermostat).

# Build Dependencies

* JDK 7+
* Maven

# Runtime Dependencies

* JRE 7+
* Mongodb 3.x+

# How to Build

```
$ mvn clean verify

```

# How to Run

## How to Start

```
$ ./distribution/target/image/bin/thermostat-mongodb.sh start
$ ./distribution/target/image/bin/thermostat-web-gateway.sh
```

This will run the Web Gateway with default configuration, listening on http://127.0.0.1:30000

## How to Stop

`Ctrl-C` to stop `thermostat-web-gateway.sh`

```
$ ./distribution/target/image/bin/thermostat-mongodb.sh stop
```

# API:

For API see the Swagger YAML specifications in `./services/*/src/main/resources`

Alternatively, visit the Swagger UI hosted by the Web Gateway at `http://127.0.0.1:30000/doc/index.html` and load specifications provided by the services. For example: `http://127.0.0.1:30000/doc/index.html?url=http://127.0.0.1:30000/jvm-gc/0.0.2/doc/jvm-gc-swagger.yaml`

# How to debug after building

## Run debug script

This runs Thermostat Web Gateway listening on socket 5005 for a debugger to connect.

```
$ ./distribution/target/image/bin/thermostat-web-gateway-debug.sh
```

## Run IdeLauncher class

This class is meant to run from IDE (Eclipse, Intellij, etc.) with configuration setting the environment variable `THERMOSTAT_GATEWAY_HOME` to `distribution/target/image`
```
