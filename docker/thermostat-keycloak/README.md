# Thermostat Keycloak Docker Image

# How to Use

## Build image

```
$ docker build -t thermostat-keycloak .
```

## Run container

```
$ docker run --name thermostat-keycloak thermostat-keycloak
```

## Settings

### Admin Console via Browser

Find the IP address of the docker container via:
```
$ docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' thermostat-keycloak
```

The port is 8080 and the address is /auth.

E.g. http://172.17.0.2:8080/auth

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
$ docker run --name thermsotat-keycloak thermostat-keycloak
```

### Modify Thermostat Web Gateway with Keycloak credentials and services

Use a browser to access the Keycloak server's admin console at E.g.
http://172.17.0.2:8080/auth (use the IP/Port of your container)
Login with the Administrative User Credentials listed above. Via
(Clients -> thermostat-bearer -> Installation, selecet the Format option
'Keycloak OIDC JSON' and download the 'keycloak.json' file into
`image/etc/keycloak.json`

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

E.g in a bash script:

```
$ RESULT=`curl --data "grant_type=password&client_id=thermostat-web-client&username=tms-user&password=tms-pass" http://172.17.0.2:8080/auth/realms/thermostat/protocol/openid-connect/token`

$ TOKEN=`echo $RESULT | sed 's/.*access_token":"//g' | sed 's/".*//g'`

$ curl -H "Authorization: bearer $TOKEN" "http://localhost:30000/jvm-gc/0.0.1"
```



