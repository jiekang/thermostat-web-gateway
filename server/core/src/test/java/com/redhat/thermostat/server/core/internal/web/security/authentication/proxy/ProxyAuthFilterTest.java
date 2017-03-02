package com.redhat.thermostat.server.core.internal.web.security.authentication.proxy;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class ProxyAuthFilterTest {
    private ProxyAuthFilter proxyAuthFilter;

    private ContainerRequestContext crq;

    private final String userName = "user";

    private final ArgumentCaptor<SecurityContext> sc = ArgumentCaptor.forClass(SecurityContext.class);

    @Before
    public void setup() {
        crq = mock(ContainerRequestContext.class);
        when(crq.getHeaderString("X-SSSD-REMOTE-USER")).thenReturn(userName);
        String groups = "a:b";
        when(crq.getHeaderString("X-SSSD-REMOTE-USER-GROUPS")).thenReturn(groups);

        proxyAuthFilter = new ProxyAuthFilter();
    }

    @Test (expected = NotAuthorizedException.class)
    public void testNoUserHeader() throws IOException {
        ContainerRequestContext crq = mock(ContainerRequestContext.class);
        proxyAuthFilter.filter(crq);
    }

    @Test
    public void testGetUserPrincipalName() throws IOException {
        proxyAuthFilter.filter(crq);
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(userName, sc.getValue().getUserPrincipal().getName());
    }

    @Test
    public void testIsUserInRole() throws IOException {
        proxyAuthFilter.filter(crq);
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(true, sc.getValue().isUserInRole("a"));
        assertEquals(true, sc.getValue().isUserInRole("b"));
    }

    @Test
    public void testIsUserNotInRole() throws IOException {
        proxyAuthFilter.filter(crq);
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(false, sc.getValue().isUserInRole("unknown-role"));
    }

    @Test
    public void testIsSecure() throws IOException {
        proxyAuthFilter.filter(crq);
        verify(crq).setSecurityContext(sc.capture());

        assertEquals(true, sc.getValue().isSecure());
    }

    @Test
    public void testGetAuthenticationScheme() throws IOException {
        proxyAuthFilter.filter(crq);
        verify(crq).setSecurityContext(sc.capture());

        assertEquals("Proxy", sc.getValue().getAuthenticationScheme());
    }
}
