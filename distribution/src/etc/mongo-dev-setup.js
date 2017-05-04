db = db.getSiblingDB("thermostat")
var v = db.version()
var majorVersion = v.substring(0, v.indexOf('.'))
var minorMicro = v.substr(v.indexOf('.') + 1)
var minorVersion = minorMicro.substr(0, minorMicro.indexOf('.'))
try {
    if ( majorVersion < 2 || ( majorVersion == 2 && minorVersion <= 2 ) ) {
        // mongodb version 2.2 and below don't have the third argument.
        // this should create the user as read + write.
        db.addUser("mongodevuser","mongodevpassword")
    } else if ( majorVersion == 2 && minorVersion <= 4 ) {
        db.addUser({ user: "mongodevuser", pwd: "mongodevpassword", roles: [ "readWrite" ] })
    } else if ( majorVersion == 2 ) {
        db.createUser({ user: "mongodevuser", pwd: "mongodevpassword", roles: [ "readWrite" ] })
    } else if ( majorVersion == 3 ) {
        db = db.getSiblingDB("admin")
        // role 'hostManager' is required to permit the fsync operation after creating the users
        db.createUser({ user: "thermostat-admin", pwd: "mongodevpassword", roles: [ "dbOwner", "userAdminAnyDatabase", "hostManager" ] })
        db.auth("thermostat-admin", "mongodevpassword")
        db = db.getSiblingDB("thermostat")
        db.createUser({ user: "mongodevuser", pwd: "mongodevpassword", roles: [ "readWrite" ] })
        // on Windows (which is mongodb 3 and up only), ensure the new users are written to disk
        // (Windows has a hard shutdown sometimes)
        db = db.getSiblingDB("admin")
        db.runCommand({ fsync: 1, async: false })
    } else {
        throw "Unknown mongo version: " + v
    }
    // Exit with a success return code.
    quit(0)
} catch (e) {
    print(e)
    // Be sure to leave the script with a non-zero code
    quit(1)
}
