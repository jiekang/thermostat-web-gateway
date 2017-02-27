package com.redhat.thermostat.server.core.internal.security.authorization;

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

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testAsNoneBase() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100");

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testNamespacesAll() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testNamespacesNone() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems");

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testNamespacesSpecificYes() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testNamespacesSpecificNo() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testSystemsAll() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testSystemsNone() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testSystemsSpecificYes() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testSystemsSpecificNo() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testAgentsAll() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all/agents/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testAgentsNone() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all/agents/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testAgentsSpecificYes() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific/agents/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-agents-specific")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testAgentsSpecificNo() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific/agents/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-agents-specific")).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testJvmsAll() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all/agents/all/jvms/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-jvms-all")).thenReturn(true);


        roleAuthFilter.filter(containerRequestContext);
    }

    @Test(expected = NotAuthorizedException.class)
    public void testJvmsNone() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/any/systems/all/agents/all/jvms/all");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(true);
        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(true);

        roleAuthFilter.filter(containerRequestContext);
    }

    @Test
    public void testJvmsSpecificYes() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific/agents/specific/jvms/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-all")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-agents-specific")).thenReturn(true);


        when(securityContext.isUserInRole("thermostat-jvms-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-jvms-specific")).thenReturn(true);


        roleAuthFilter.filter(containerRequestContext);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testJvmsSpecificNo() throws IOException {
        when(uriInfo.getPath()).thenReturn("/api/v100/specific/systems/specific/agents/specific/jvms/specific");

        when(securityContext.isUserInRole("thermostat-namespaces-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-systems-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-systems-specific")).thenReturn(true);

        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-agents-specific")).thenReturn(true);


        when(securityContext.isUserInRole("thermostat-agents-all")).thenReturn(false);
        when(securityContext.isUserInRole("thermostat-agents-specific")).thenReturn(false);

        roleAuthFilter.filter(containerRequestContext);
    }
}
