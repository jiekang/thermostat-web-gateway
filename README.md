[![Build Status](https://travis-ci.org/jiekang/thermostat-web-gateway.svg?branch=master)](https://travis-ci.org/jiekang/thermostat-web-gateway)

# Thermostat Web Gateway

This is a RESTful API server for [Thermostat](http://icedtea.classpath.org/thermostat).

* common: Contains packages used by other modules
* distribution: Contains build assembly and default configuration files to create runnable program
* server: Contains CoreServer to handle deploying microservices
* services: Contains microservices deployable via server:CoreServer, or any Servlet supporting server such as Wildfly, Jetty, Glassfish or Tomcat
* services/jvm-gc: Contains microservice for accessing jvm-gc data from Mongodb backend
* services/jvm-memory: Contains microservice for accessing jvm-memory data from Mongodb backend

* thermostat-mongodb: Contains standalone Web Gateway to interface with Thermostat data placed in Mongodb
* endpoints/cmd-channel: Contains standalone Command Channel via Web Sockets implementation for Client, Agent communication


# Build Dependencies

* JDK 7+
* Maven

# Runtime Dependencies

* JRE 7+
* Mongodb
* Thermostat

# How to use

To build:

```
$ mvn clean verify

```

To run:

```
$ ./distribution/target/image/bin/thermostat-web-gateway.sh
```

The jvm-memory and jvm-gc services expect mongodb to be running with db 'thermostat', username 'mongodevuser' and password 'mongodevpassword'.

This can be setup with Thermostat:

In thermostat installation ./bin folder:

```
$ ./thermostat-devsetup
$ ./thermostat web-storage-service
```

Try:

```
$ curl "http://localhost:30000/jvm-memory/0.0.1
$ curl "http://localhost:30000/jvm-gc/0.0.1
```

For API see:

```
services/*/*-swagger.json
```
