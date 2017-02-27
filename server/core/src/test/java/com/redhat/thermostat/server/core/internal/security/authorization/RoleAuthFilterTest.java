package com.redhat.thermostat.server.core.internal.security.authorization;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

public class RoleAuthFilterTest {
    private RoleAuthFilter roleAuthFilter;
    private ContainerRequestContext containerRequestContext;
    private UriInfo uriInfo;
    private SecurityContext securityContext;

    @Before
    public void setup() {
        containerRequestContext = mock(ContainerRequestContext.class);

        uriInfo = mock(UriInfo.class);
        when(containerRequestContext.getUriInfo()).thenReturn(uriInfo);

        securityContext = mock(SecurityContext.class);
        when(containerRequestContext.getSecurityContext()).thenReturn(securityContext);

        roleAuthFilter = new RoleAuthFilter();
    }

    @Test
    public void testAsAdmin() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/systems/all/agents/all/jvms/all");

        when(securityContext.isUserInRole("thermostat-admin")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testAsNone() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/systems/all/agents/all/jvms/all");

        when(securityContext.isUserInRole(anyString())).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testAsNoneBase() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100");

        when(securityContext.isUserInRole(anyString())).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testNamespaceAll() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testNamespaceSpecificYes() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }
    @Test (expected = NotAuthorizedException.class)
    public void testNamespaceSpecificNo() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testSystemAll() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/systems/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

}
