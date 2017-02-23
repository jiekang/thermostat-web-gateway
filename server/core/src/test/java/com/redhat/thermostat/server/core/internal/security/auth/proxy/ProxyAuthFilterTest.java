package com.redhat.thermostat.server.core.internal.security.auth.proxy;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashSet;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.redhat.thermostat.server.core.internal.security.UserStore;
import com.redhat.thermostat.server.core.internal.security.WebUser;

public class ProxyAuthFilterTest {
    private ProxyAuthFilter proxyAuthFilter;

    private UserStore userStore;
    private WebUser user;
    private ContainerRequestContext crq;

    private String userName = "user";
    private String groups = "a:b";

    private ArgumentCaptor<SecurityContext> sc = ArgumentCaptor.forClass(SecurityContext.class);

    @Before
    public void setup() {
        userStore = mock(UserStore.class);
        user = new ProxyWebUser(userName, new HashSet<String>());

        when(userStore.getUser(userName)).thenReturn(user);

        crq = mock(ContainerRequestContext.class);
        when(crq.getHeaderString("X-SSSD-REMOTE-USER")).thenReturn(userName);
        when(crq.getHeaderString("X-SSSD-REMOTE-USER-GROUPS")).thenReturn(groups);

        proxyAuthFilter = new ProxyAuthFilter(userStore);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testNoUserHeader() throws IOException {
        ContainerRequestContext crq = mock(ContainerRequestContext.class);
        proxyAuthFilter.filter(crq);
    }

    @Test (expected = NotAuthorizedException.class)
    public void testNoUserInStore() throws IOException {
        ContainerRequestContext crq = mock(ContainerRequestContext.class);
        when(crq.getHeaderString("X-SSSD-REMOTE-USER")).thenReturn("unknown-user");

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
