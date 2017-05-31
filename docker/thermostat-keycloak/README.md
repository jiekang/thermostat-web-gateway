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

### Windows notes for Keycloak

Docker and VirtualBox do not play well together (you can only run one or the other); you need to use Docker Toolbox to avoid a Hyper-V conflict.
The Keycloak image must be built from the Docker Quickstart terminal (MingW), not from a Windows command shell.

An alternative is to run Keycloak natively in Windows.  A Keycloak configuration script is provided for that purpose.

(tested with keycloak-3.1.0.Final.zip from  http://www.keycloak.org/downloads.html)

1) Download Keycloak, and unzip the installation file.
2) set KEYCLOAK_HOME to the root of the unzipped Keycloak installation
3) set JAVA_HOME to the root of a valid Java runtime installation
4) while Keycloak is not running, run (web-gateway)\docker\thermostat-keycloak\setup-keycloak.cmd
   (this will create an admin user, temporarily bring up Keycloak in another window, and set up the Thermostat realm)
4a) optionally, change the server port from the default of 8080.
    When running Keycloak in standalone mode, this is configured near the bottom of (keycloak)\standalone\configuration\standalone.xml.
    (Search for socket-binding-group)
5) start Keycloak normally: %KEYCLOAK_HOME%\bin\standalone.bat



