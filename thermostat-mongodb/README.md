# Thermostat Web Server

## Dependencies:

Mongodb

## How To Use

The Web Server will attempt to connect to mongod://127.0.0.1:27518 'thermostat'
with credentials 'mongodevuser' and 'mongodevpassword'. A valid mongod instance
can be created via the Thermostat project, running:

```
$ thermostat-devsetup
$ thermostata storage --start
```

Start the Web Server via:

```
$ java -jar distribution/target/thermostat-mongodb-gateway-distribution-1.99.12-SNAPSHOT-assemble-jar.jar
```

The default setup will listen on http://localhost:26000/api/v100

## API

See v1.0.0.yaml

## Collections in Thermostat HEAD

agent-config
backend-info
cpu-stats
host-info
memory-stats
network-info
numa-host-info
numa-stat
profile-info
profile-status
schema-info
vm-class-stats
vm-compiler-stats
vm-cpu-stats
vm-deadlock-data
vm-gc-stats
vm-heap-info
vm-info
vm-io-stats
vm-jmx-notification
vm-jmx-notification-status
vm-memory-stats
vm-numa-stats
vm-shenandoah-stats
vm-thread-harvesting
vm-thread-lock
vm-thread-session
vm-thread-state
vm-thread-summary
vm-tlab-stats
