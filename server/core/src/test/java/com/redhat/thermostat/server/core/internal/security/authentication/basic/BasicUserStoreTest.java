package com.redhat.thermostat.server.core.internal.security.authentication.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.redhat.thermostat.server.core.internal.security.WebUser;

public class BasicUserStoreTest {

    @Test
    public void testLoadBasicUser() {
        String userName = "user";
        String userInfo = "password,a,b";

        Map<String,String> entries = new HashMap<>();
        entries.put(userName, userInfo);

        BasicUserStore userStore = new BasicUserStore(entries);

        BasicWebUser user = userStore.getUser(userName);

        assertTrue(user != null);

        assertTrue(user.isUserInRole("a"));
        assertTrue(user.isUserInRole("b"));
        assertEquals(userName, user.getUsername());
        assertEquals("password", ((BasicWebUser) user).getPassword());
    }

    @Test
    public void testLoadMultipleBasicUser() {
        Map<String,String> entries = new HashMap<>();
        int num = 4;
        for (int i = 0; i < num; i++) {
            String userName = "user" + i;
            String userInfo = "password" + i + ",a,b," + i;

            entries.put(userName, userInfo);
        }

        BasicUserStore userStore = new BasicUserStore(entries);

        for (int i = 0; i < num; i++) {
            String userName = "user" + i;
            String password = "password" + i;
            BasicWebUser user = userStore.getUser(userName);

            assertTrue(user != null);

            assertTrue(user.isUserInRole("a"));
            assertTrue(user.isUserInRole("b"));
            assertTrue(user.isUserInRole(String.valueOf(i)));
            assertEquals(userName, user.getUsername());
            assertEquals(password, ((BasicWebUser) user).getPassword());
        }
    }
}
