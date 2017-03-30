# Thermostat Web Gateway - Microservices Branch

This is a Microservice-Architecture prototype for Thermostat Web Gateway.

* core: Contains packages used by other modules.
* distribution: Contains build assembly and default configuration files to create runnable program.
* s2: Contains CoreServer to handle deploying microservices
* services: Contains microservices deployable via s2:CoreServer, or any Servlet supporting program
            such as Wildfly, Jetty, Glassfish or Tomcat
* services/jvm-gc: Contains microservice for accessing jvm-gc data from Mongodb backend
* services/jvm-memory: Contains microservice for accessing jvm-memory data from Mongodb backend

* server: Contains Maven project for Thermostat Web Gateway


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
