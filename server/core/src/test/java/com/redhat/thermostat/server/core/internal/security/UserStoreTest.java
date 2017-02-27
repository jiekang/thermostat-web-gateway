package com.redhat.thermostat.server.core.internal.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.redhat.thermostat.server.core.internal.security.authentication.basic.BasicWebUser;
import com.redhat.thermostat.server.core.internal.security.authentication.proxy.ProxyWebUser;

public class UserStoreTest {

    @Test
    public void testProxyUser() {
        String userName = "user";
        String userInfo = "proxy,a,b";
        Map<String,String> entries = new HashMap<>();
        entries.put(userName, userInfo);

        UserStore userStore = UserStore.get().load(entries);

        WebUser user = userStore.getUser(userName);

        assertTrue(user instanceof ProxyWebUser);

        assertTrue(user.isUserInRole("a"));
        assertTrue(user.isUserInRole("b"));
        assertEquals(userName, user.getUsername());
    }

    @Test
    public void testBasicUser() {
        String userName = "user";
        String userInfo = "basic,password,a,b";

        Map<String,String> entries = new HashMap<>();
        entries.put(userName, userInfo);

        UserStore userStore = UserStore.get().load(entries);

        WebUser user = userStore.getUser(userName);

        assertTrue(user instanceof BasicWebUser);

        assertTrue(user.isUserInRole("a"));
        assertTrue(user.isUserInRole("b"));
        assertEquals(userName, user.getUsername());
        assertEquals("password", ((BasicWebUser) user).getPassword());
    }
}
