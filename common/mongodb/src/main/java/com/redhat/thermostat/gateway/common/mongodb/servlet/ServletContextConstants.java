package com.redhat.thermostat.gateway.common.mongodb.servlet;

import com.redhat.thermostat.gateway.common.core.servlet.GlobalConstants;

public interface ServletContextConstants {
    /**
     * Attribute name of the mongodb client reference
     */
    final String MONGODB_CLIENT_ATTRIBUTE = GlobalConstants.GATEWAY_PREFIX + ".common.mongodb.client";
}
