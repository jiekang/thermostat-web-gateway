package com.redhat.thermostat.server.core.web;

import org.glassfish.jersey.server.ResourceConfig;
import java.util.Map;
import com.redhat.thermostat.server.core.internal.web.configuration.ServerConfiguration;
import io.swagger.jaxrs.config.BeanConfig;

public class CustomResourceConfig extends ResourceConfig {
    public CustomResourceConfig(Map<String, String> serverConfig) {

        if (serverConfig.containsKey(ServerConfiguration.SWAGGER_UI_ENABLED.toString()) &&
                serverConfig.get(ServerConfiguration.SWAGGER_UI_ENABLED.toString()).equals("true")) {
            BeanConfig beanConfig = new BeanConfig();
            beanConfig.setBasePath("/");
            beanConfig.setVersion("1.0.0");
            beanConfig.setTitle("Thermostat Web API");
            beanConfig.setLicense("GPL v2 with Classpath Exception");
            beanConfig.setLicenseUrl("http://www.gnu.org/licenses");
            // scan for JAX-RS classes from this package
            beanConfig.setResourcePackage("com.redhat.thermostat.server.core.internal.web.http");
            beanConfig.setScan(true);
        }
    }
}
