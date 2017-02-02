package com.redhat.thermostat.server.core.internal.configuration;

public class ServerConfiguration {
    public enum ServerOptions {
        SECURITY_PROXY,
        SECURITY_BASIC,
        SECURITY_BASIC_SSL,

        SERVER_PORT,
        SERVER_URL,

        SWAGGER_ENABLED,

    }

}
