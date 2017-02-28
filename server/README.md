Thermostat Web Server

Dependencies:

Thermostat
Mongodb

Run

$ mvn install

on a local Thermostat repository to place the required Thermostat dependency in
your local maven repository. Then build with:

$ mvn package


To install the Web Server into a Thermostat installation, you can unzip
"server/distribution/target/thermostat-server-distribution-1.99.12-SNAPSHOT.zip"
into "<path-to-thermostat-distribution>/image".

There is a "util/install-plugin.sh" that can be run from within the "server" folder
to unzip the distribution for you. Make sure to modify the THERMOSTAT_HOME variable
in the script to point to a valid Thermostat repository before running the script.


The Web Server will attempt to connect to mongod://127.0.0.1:27518 'thermostat'
with credentials 'mongodevuser' and 'mongodevpassword'. A valid mongod instance
can be created and started by running:

$ thermostat-devsetup
$ thermostatastorage --start


Configuration

Default configuration files are located in THERMOSTAT_HOME/etc/plugins.d/server

Either modify these configuration files or place custom files in 
USER_THERMOSTAT_HOME/etc/plugins.d/server

The properties in USER_THERMOSTAT_HOME override those in THERMOSTAT_HOME.


Authentication and Authorization

The default auth system is Proxy. The two other supported systems are Basic and None.
These two are mainly for testing purposes. Production systems should NOT use Basic or
None as the auth system as they are open for attack.

Configuration of the auth system is done via the files:
server-config.properties
basic-config.properties

None

This amounts to no authentication or authorization for requests. To enable this, modify
the 'server-config.properties' file to have no SECURITY_PROXY_URL or SECURITY_BASIC_URL

Basic

This requires requests to have BASIC authentication using a username and password. To
enable this, modify the 'server-config.properties' file to have SECURITY_BASIC_URL
property and no SECURITY_PROXY_URL property.

e.g.
SECURITY_BASIC_URL=http://localhost:8090

Proxy

This system requires a front-end server to reverse proxy requests to the Thermostat
Web Server. The front-end server will provide authentication and authorization via
for example, Kerberos and LDAP. To enable this, modify the 'server-config.properties'
file to have SECURITY_PROXY_URL property and no SECURITY_BASIC_URL property.

e.g.
SECURITY_PROXY_URL=http://localhost:8090

The Thermostat Web Server will expect requests to have the X-SSSD-REMOTE-USER header
set to the username, as well as the X-SSSD-REMOTE-USER-GROUPS header set to a colon
(:) separated list of groups attached to the username.

With Proxy configuration, the Thermostat Web Server should only listen on the local
loopback interface (localhost, 127.0.0.1) while the front-end server listens on the
network and reverse proxies requests to the Web Server.


Storage Backend

The Thermostat Web Server requires mongodb for data storage. The connection can
be configured using the file:
mongo-config.properties

The following properties are supported:

MONGO_URL : the url to connect to (required)
MONGO_DB : the db name to use (required)
MONGO_USERNAME : the username to use for authentication
MONGO_PASSWORD : the password to use for authentication

Authentication via username and password is optional and will not be done unless
both MONGO_USERNAME and MONGO_PASSWORD are specified.

