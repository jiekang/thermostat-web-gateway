Thermostat Web Server

Dependencies:

Thermostat
Mongodb

Run

$ mvn install

on a local Thermostat repository to place the required Thermostat dependency in your local maven repository. Then build with:

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
$ thermostat storage --start
