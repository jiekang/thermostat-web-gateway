# Thermostat Keycloak Docker Image

# How to Use

## Build image (optional)

```
$ docker build --rm -t icedtea/dev-thermostat-keycloak .
```

## Run container

```
$ docker run -d -p 127.0.0.1:8900:8080 icedtea/dev-thermostat-keycloak
```

## Settings

### Admin Console via Browser

Visit URL http://127.0.0.1:8900/auth and click on
Administration Console.

### Administative User

Username: tms-admin
Password: tms-admin

### Thermostat User

Username: tms-user
Password: tms-pass


### Clients

There are two clients of interest, `thermostat-bearer` and `thermostat-web-client`.
`thermostat-bearer` can be used to generate a keycloak.json file for the Thermostat
Web Gateway. `thermostat-web-client` can be used as the client ID for JWT token
accesses, along with the Thermostat User credentials.


## Testing Thermostat Web Gateway via Curl

### Run Thermostat
```
$ ./thermostat-devsetup
$ ./thermostat web-storage-service
```

### Run Keycloak server
```
$ docker run -d -p 127.0.0.1:8900:8080 icedtea/dev-thermostat-keycloak
```

### Modify Thermostat Web Gateway with Keycloak credentials and services

Generate and install keycloak.json file:

```
$ ./docker/thermostat-keycloak/get_keycloak_json.sh 
Waiting for Keycloak container to become ready ...done.
./docker/thermostat-keycloak/../../distribution/target/image/etc/keycloak.json generated and installed.
```

Modify `/image/etc/gloabl-config.properties` and add the property
`file|KEYCLOAK_CONFIG=keycloak.json`.

To enable Keycloak for a service, add the property `SECURITY_KEYCLOAK=true'
to the respective `service-config.properties` file; e.g.
`image/etc/jvm-gc/service-config.properties`

### Run Thermostat Web Gateway
```
$ image/bin/thermostat-web-gateway.sh
```

### Test via Curl

For example, in a bash script:

```
$ RESULT=`curl -s --data "grant_type=password&client_id=thermostat-web-client&username=tms-user&password=tms-pass" http://127.0.0.1:8900/auth/realms/thermostat/protocol/openid-connect/token`

$ TOKEN=`echo $RESULT | sed 's/.*access_token":"//g' | sed 's/".*//g'`

$ curl -H "Authorization: bearer $TOKEN" "http://localhost:30000/jvm-gc/0.0.1"
```

