package com.redhat.thermostat.gateway.common.core;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

abstract class ConfigurationTest {

    protected String getTestRoot() {
        URL rootUrl = ConfigurationTest.class.getResource("/test_root");
        return decodeFilePath(rootUrl);
    }

    private String decodeFilePath(URL url) {
        try {
            // Spaces are encoded as %20 in URLs. Use URLDecoder.decode() so
            // as to handle cases like that.
            return URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 not supported, huh?");
        }
    }
}
