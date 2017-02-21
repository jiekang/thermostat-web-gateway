package io.swagger.api.factories;

import io.swagger.api.DefaultApiService;
import io.swagger.api.impl.DefaultApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaJerseyServerCodegen", date = "2017-02-21T16:01:56.111Z")
public class DefaultApiServiceFactory {
    private final static DefaultApiService service = new DefaultApiServiceImpl();

    public static DefaultApiService getDefaultApi() {
        return service;
    }
}
