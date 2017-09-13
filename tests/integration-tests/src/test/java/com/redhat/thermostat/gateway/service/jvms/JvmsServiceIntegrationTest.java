/*
 * Copyright 2012-2017 Red Hat, Inc.
 *
 * This file is part of Thermostat.
 *
 * Thermostat is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your
 * option) any later version.
 *
 * Thermostat is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Thermostat; see the file COPYING.  If not see
 * <http://www.gnu.org/licenses/>.
 *
 * Linking this code with other modules is making a combined work
 * based on this code.  Thus, the terms and conditions of the GNU
 * General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this code give
 * you permission to link this code with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also
 * meet, for each linked independent module, the terms and conditions
 * of the license of that module.  An independent module is a module
 * which is not derived from or based on this code.  If you modify
 * this code, you may extend this exception to your version of the
 * library, but you are not obligated to do so.  If you do not wish
 * to do so, delete this exception statement from your version.
 */

package com.redhat.thermostat.gateway.service.jvms;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.redhat.thermostat.gateway.tests.integration.MongoIntegrationTest;
import com.redhat.thermostat.gateway.tests.utils.ContentWrapper;
import com.redhat.thermostat.gateway.tests.utils.ContentWrapper.Array;
import com.redhat.thermostat.gateway.tests.utils.ContentWrapper.Map;
import com.redhat.thermostat.gateway.tests.utils.HttpTestUtil;
import com.redhat.thermostat.gateway.tests.utils.ResponseSetWrapper;

public class JvmsServiceIntegrationTest extends MongoIntegrationTest {

    private static final String serviceName = "jvms";
    private static final String serviceVersion = "0.0.1";
    private static final String jvmsUrl = baseUrl + "/" + serviceName + "/" + serviceVersion;

    private static final ContentWrapper postDataWrapper = new ContentWrapper().addToResponse(
         Map.of("agentId", "aid",
                "startTime", Map.of("$numberLong", "1495727607481"),
                "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                "uid", 1000,
                "jvmName", "vm",
                "stopTime", Map.of("$numberLong", "-9223372036854775808"),
                "username", "user",
                "jvmInfo", "mixed mode",
                "environment", Array.of(
                                    Map.of("key", "PATH",
                                           "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                    Map.of("key", "XAUTHORITY",
                                           "value", "/run/user/1000/gdm/Xauthority"),
                                    Map.of("key", "GDMSESSION",
                                           "value", "i3"),
                                    Map.of("key", "fish_greeting",
                                           "value", ""),
                                    Map.of("key", "TERM",
                                           "value", "xterm-256color"),
                                    Map.of("key", "DARWIN_MODE",
                                           "value", "0"),
                                    Map.of("key", "LANG",
                                           "value", "en_US.UTF-8"),
                                    Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                           "value", "unix:path=/run/user/1000/bus"),
                                    Map.of("key", "XDG_SESSION_ID",
                                           "value", "2"),
                                    Map.of("key", "XDG_SESSION_TYPE",
                                           "value", "x11"),
                                    Map.of("key", "XDG_CURRENT_DESKTOP",
                                           "value", "i3"),
                                    Map.of("key", "DISPLAY",
                                           "value", ":0"),
                                    Map.of("key", "CYGWIN_MODE",
                                           "value", "0"),
                                    Map.of("key", "COLORTERM",
                                           "value", "truecolor"),
                                    Map.of("key", "_",
                                           "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                "javaVersion", "1.8.0_131",
                "jvmVersion", "25.131-b12",
                "jvmId", "jid1",
                "javaCommandLine", "j cl",
                "vmArguments", "-Djline.log.jul=true",
                "jvmPid", 1,
                "lastUpdated", Map.of("$numberLong", "333"),
                "mainClass", "mc"),
                Map.of("agentId", "aid",
                        "startTime", Map.of("$numberLong", "1495727607481"),
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "uid", 1000,
                        "jvmName", "vm",
                        "stopTime", Map.of("$numberLong", "1495727607482"),
                        "username", "user",
                        "jvmInfo", "mixed mode",
                        "environment", Array.of(
                                           Map.of("key", "PATH",
                                                   "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                           Map.of("key", "XAUTHORITY",
                                                   "value", "/run/user/1000/gdm/Xauthority"),
                                           Map.of("key", "GDMSESSION",
                                                   "value", "i3"),
                                           Map.of("key", "fish_greeting",
                                                   "value", ""),
                                           Map.of("key", "TERM",
                                                   "value", "xterm-256color"),
                                           Map.of("key", "DARWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "LANG",
                                                   "value", "en_US.UTF-8"),
                                           Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                                   "value", "unix:path=/run/user/1000/bus"),
                                           Map.of("key", "XDG_SESSION_ID",
                                                   "value", "2"),
                                           Map.of("key", "XDG_SESSION_TYPE",
                                                   "value", "x11"),
                                           Map.of("key", "XDG_CURRENT_DESKTOP",
                                                   "value", "i3"),
                                           Map.of("key", "DISPLAY",
                                                   "value", ":0"),
                                           Map.of("key", "CYGWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "COLORTERM",
                                                   "value", "truecolor"),
                                           Map.of("key", "_",
                                                   "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                        "javaVersion", "1.8.0_131",
                        "jvmVersion", "25.131-b12",
                        "jvmId", "jid2",
                        "javaCommandLine", "j cl",
                        "vmArguments", "-Djline.log.jul=true",
                        "jvmPid", 2,
                        "lastUpdated", Map.of("$numberLong", "333"),
                        "mainClass", "mc"));

    private final String postData = postDataWrapper.getJsonResponseArray();
    private final String postDataWithSystemId = postData.substring(0, postData.length() - 2) + ", \"systemId\" : \"invalid\"}]";

    public JvmsServiceIntegrationTest() {
        super(jvmsUrl, "jvm-info");
    }

    @Override
    public String getServiceVersion() {
        return serviceVersion;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Test
    public void testGetEmpty() throws InterruptedException, TimeoutException, ExecutionException {
        String url = jvmsUrl + "/systems/1";
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, response.getStatus());
        String expected = "{\"response\":[]}";
        assertEquals(expected, response.getContentAsString());
    }

    @Test
    public void testIsAliveFalseGetJvmInfo() throws InterruptedException, ExecutionException, TimeoutException {
        String postUrl = jvmsUrl + "/systems/1";
        String getUrl = jvmsUrl + "/systems/1/jvms/jid2";

        ContentResponse postResponse = client.newRequest(postUrl).method(HttpMethod.POST)
                .content(new StringContentProvider(postDataWrapper.getJsonResponseArray()), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ContentResponse getResponse = client.newRequest(getUrl).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("jvmName", "vm",
                        "agentId", "aid",
                        "javaCommandLine", "j cl",
                        "lastUpdated", 333,
                        "javaVersion", "1.8.0_131",
                        "jvmId", "jid2",
                        "isAlive", false,
                        "systemId", "1",
                        "jvmPid", 2,
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "stopTime", 1495727607482L,
                        "vmArguments", "-Djline.log.jul=true",
                        "jvmInfo", "mixed mode",
                        "jvmVersion", "25.131-b12",
                        "username", "user",
                        "mainClass", "mc",
                        "environment", Array.of(
                                Map.of("key", "PATH",
                                        "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                Map.of("key", "XAUTHORITY",
                                        "value", "/run/user/1000/gdm/Xauthority"),
                                Map.of("key", "GDMSESSION",
                                        "value", "i3"),
                                Map.of("key", "fish_greeting",
                                        "value", ""),
                                Map.of("key", "TERM",
                                        "value", "xterm-256color"),
                                Map.of("key", "DARWIN_MODE",
                                        "value", "0"),
                                Map.of("key", "LANG",
                                        "value", "en_US.UTF-8"),
                                Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                        "value", "unix:path=/run/user/1000/bus"),
                                Map.of("key", "XDG_SESSION_ID",
                                        "value", "2"),
                                Map.of("key", "XDG_SESSION_TYPE",
                                        "value", "x11"),
                                Map.of("key", "XDG_CURRENT_DESKTOP",
                                        "value", "i3"),
                                Map.of("key", "DISPLAY",
                                        "value", ":0"),
                                Map.of("key", "CYGWIN_MODE",
                                        "value", "0"),
                                Map.of("key", "COLORTERM",
                                        "value", "truecolor"),
                                Map.of("key", "_",
                                        "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                        "startTime", 1495727607481L,
                        "uid", 1000));
        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testIsAliveFalseGetJvmTree() throws InterruptedException, ExecutionException, TimeoutException {
        String postUrl = jvmsUrl + "/systems/1";
        String treeUrl = jvmsUrl + "/tree";

        ContentResponse postResponse = client.newRequest(postUrl).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?aliveOnly=false&offset=1";
        ContentResponse response = client.newRequest(treeUrl + query).method(HttpMethod.GET).send();
        assertEquals(200, response.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("systemId", "1",
                        "jvms", Array.of(Map.of("jvmPid", 2,
                        "uid", 1000,
                        "jvmVersion", "25.131-b12",
                        "javaVersion", "1.8.0_131",
                        "lastUpdated", Map.of("$numberLong", "333"),
                        "vmArguments", "-Djline.log.jul=true",
                        "jvmId", "jid2",
                        "startTime", Map.of("$numberLong", "1495727607481"),
                        "username", "user",
                        "systemId", "1",
                        "stopTime", Map.of("$numberLong", "1495727607482"),
                        "environment", Array.of(
                                           Map.of("value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin",
                                                   "key", "PATH"),
                                           Map.of("value", "/run/user/1000/gdm/Xauthority",
                                                   "key", "XAUTHORITY"),
                                           Map.of("value", "i3",
                                                   "key", "GDMSESSION"),
                                           Map.of("value", "",
                                                   "key", "fish_greeting"),
                                           Map.of("value", "xterm-256color",
                                                   "key", "TERM"),
                                           Map.of("value", "0",
                                                   "key", "DARWIN_MODE"),
                                           Map.of("value", "en_US.UTF-8",
                                                   "key", "LANG"),
                                           Map.of("value", "unix:path=/run/user/1000/bus",
                                                   "key", "DBUS_SESSION_BUS_ADDRESS"),
                                           Map.of("value", "2",
                                                   "key", "XDG_SESSION_ID"),
                                           Map.of("value", "x11",
                                                   "key", "XDG_SESSION_TYPE"),
                                           Map.of("value", "i3",
                                                   "key", "XDG_CURRENT_DESKTOP"),
                                           Map.of("value", ":0",
                                                   "key", "DISPLAY"),
                                           Map.of("value", "0",
                                                   "key", "CYGWIN_MODE"),
                                           Map.of("value", "truecolor",
                                                   "key", "COLORTERM"),
                                           Map.of("value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java",
                                                   "key", "_")),
                        "jvmInfo", "mixed mode",
                        "jvmName", "vm",
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "isAlive", false,
                        "mainClass", "mc",
                        "javaCommandLine", "j cl",
                        "agentId", "aid")))
        );

        expectedContentWrapper.matchJsonOrThrow(response.getContentAsString());
    }

    @Test
    public void testIsAliveTrueGetJvmInfos() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postDataWrapper.getJsonResponseArray()), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "/jvms/jid1";
        ContentResponse getResponse = client.newRequest(url + query).method(HttpMethod.GET).send();

        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "mainClass", "mc",
                        "jvmId", "jid1",
                        "lastUpdated", 333,
                        "javaCommandLine", "j cl",
                        "vmArguments", "-Djline.log.jul=true",
                        "isAlive", true,
                        "uid", 1000,
                        "username", "user",
                        "jvmVersion", "25.131-b12",
                        "environment", Array.of(
                                           Map.of("key", "PATH",
                                                   "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                           Map.of("key", "XAUTHORITY",
                                                   "value", "/run/user/1000/gdm/Xauthority"),
                                           Map.of("key", "GDMSESSION",
                                                   "value", "i3"),
                                           Map.of("key", "fish_greeting",
                                                   "value", ""),
                                           Map.of("key", "TERM",
                                                   "value", "xterm-256color"),
                                           Map.of("key", "DARWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "LANG",
                                                   "value", "en_US.UTF-8"),
                                           Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                                   "value", "unix:path=/run/user/1000/bus"),
                                           Map.of("key", "XDG_SESSION_ID",
                                                   "value", "2"),
                                           Map.of("key", "XDG_SESSION_TYPE",
                                                   "value", "x11"),
                                           Map.of("key", "XDG_CURRENT_DESKTOP",
                                                   "value", "i3"),
                                           Map.of("key", "DISPLAY",
                                                   "value", ":0"),
                                           Map.of("key", "CYGWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "COLORTERM",
                                                   "value", "truecolor"),
                                           Map.of("key", "_",
                                                   "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                        "startTime", 1495727607481L,
                        "jvmName", "vm",
                        "jvmInfo", "mixed mode",
                        "agentId", "aid",
                        "systemId", "1",
                        "javaVersion", "1.8.0_131",
                        "stopTime", -9223372036854775808L,
                        "jvmPid", 1)
        );

        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testPost() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postDataWrapper.getJsonResponseArray()), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("jvmName", "vm",
                        "lastUpdated", 333,
                        "stopTime", -9223372036854775808L,
                        "mainClass", "mc",
                        "vmArguments", "-Djline.log.jul=true",
                        "jvmPid", 1,
                        "uid", 1000,
                        "jvmVersion", "25.131-b12",
                        "javaVersion", "1.8.0_131",
                        "startTime", 1495727607481L,
                        "javaCommandLine", "j cl",
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "username", "user",
                        "agentId", "aid",
                        "systemId", "1",
                        "jvmId", "jid1",
                        "environment", Array.of(
                                           Map.of("value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin",
                                                   "key", "PATH"),
                                           Map.of("value", "/run/user/1000/gdm/Xauthority",
                                                   "key", "XAUTHORITY"),
                                           Map.of("value", "i3",
                                                   "key", "GDMSESSION"),
                                           Map.of("value", "",
                                                   "key", "fish_greeting"),
                                           Map.of("value", "xterm-256color",
                                                   "key", "TERM"),
                                           Map.of("value", "0",
                                                   "key", "DARWIN_MODE"),
                                           Map.of("value", "en_US.UTF-8",
                                                   "key", "LANG"),
                                           Map.of("value", "unix:path=/run/user/1000/bus",
                                                   "key", "DBUS_SESSION_BUS_ADDRESS"),
                                           Map.of("value", "2",
                                                   "key", "XDG_SESSION_ID"),
                                           Map.of("value", "x11",
                                                   "key", "XDG_SESSION_TYPE"),
                                           Map.of("value", "i3",
                                                   "key", "XDG_CURRENT_DESKTOP"),
                                           Map.of("value", ":0",
                                                   "key", "DISPLAY"),
                                           Map.of("value", "0",
                                                   "key", "CYGWIN_MODE"),
                                           Map.of("value", "truecolor",
                                                   "key", "COLORTERM"),
                                           Map.of("value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java",
                                                   "key", "_")),
                        "jvmInfo", "mixed mode",
                        "isAlive", true)
        );

        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    /*
     * Verify POST does not override path parameter systemId with body systemId
     * POST /systems/1 [{"systemId":2 ... }] should result in systemId of 1, not 2
     */
    @Test
    public void testPostWithSystemId() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        // TODO - This should be using the ContentWrapper version of 'postDataWithSystemId'
        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postDataWithSystemId), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("username", "user",
                        "vmArguments", "-Djline.log.jul=true",
                        "stopTime", -9223372036854775808L,
                        "jvmName", "vm",
                        "lastUpdated", 333,
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "systemId", "1",
                        "isAlive", true,
                        "jvmId", "jid1",
                        "javaVersion", "1.8.0_131",
                        "javaCommandLine", "j cl",
                        "jvmInfo", "mixed mode",
                        "startTime", 1495727607481L,
                        "mainClass", "mc",
                        "jvmVersion", "25.131-b12",
                        "agentId", "aid",
                        "uid", 1000,
                        "environment", Array.of(Map.of("value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin",
                                "key", "PATH"),
                                Map.of("value", "/run/user/1000/gdm/Xauthority",
                                        "key", "XAUTHORITY"),
                                Map.of("value", "i3",
                                        "key", "GDMSESSION"),
                                Map.of("value", "",
                                        "key", "fish_greeting"),
                                Map.of("value", "xterm-256color",
                                        "key", "TERM"),
                                Map.of("value", "0",
                                        "key", "DARWIN_MODE"),
                                Map.of("value", "en_US.UTF-8",
                                        "key", "LANG"),
                                Map.of("value", "unix:path=/run/user/1000/bus",
                                        "key", "DBUS_SESSION_BUS_ADDRESS"),
                                Map.of("value", "2",
                                        "key", "XDG_SESSION_ID"),
                                Map.of("value", "x11",
                                        "key", "XDG_SESSION_TYPE"),
                                Map.of("value", "i3",
                                        "key", "XDG_CURRENT_DESKTOP"),
                                Map.of("value", ":0",
                                        "key", "DISPLAY"),
                                Map.of("value", "0",
                                        "key", "CYGWIN_MODE"),
                                Map.of("value", "truecolor",
                                        "key", "COLORTERM"),
                                Map.of("value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java",
                                        "key", "_")),
                        "jvmPid", 1));
        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testGetLimit() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?limit=2";
        ContentResponse getResponse = client.newRequest(url + query).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("mainClass", "mc",
                        "username", "user",
                        "systemId", "1",
                        "jvmId", "jid1",
                        "javaVersion", "1.8.0_131",
                        "agentId", "aid",
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "vmArguments", "-Djline.log.jul=true",
                        "jvmVersion", "25.131-b12",
                        "lastUpdated", 333,
                        "uid", 1000,
                        "environment", Array.of(
                                           Map.of("key", "PATH",
                                                   "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                           Map.of("key", "XAUTHORITY",
                                                   "value", "/run/user/1000/gdm/Xauthority"),
                                           Map.of("key", "GDMSESSION",
                                                   "value", "i3"),
                                           Map.of("key", "fish_greeting",
                                                   "value", ""),
                                           Map.of("key", "TERM",
                                                   "value", "xterm-256color"),
                                           Map.of("key", "DARWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "LANG",
                                                   "value", "en_US.UTF-8"),
                                           Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                                   "value", "unix:path=/run/user/1000/bus"),
                                           Map.of("key", "XDG_SESSION_ID",
                                                   "value", "2"),
                                           Map.of("key", "XDG_SESSION_TYPE",
                                                   "value", "x11"),
                                           Map.of("key", "XDG_CURRENT_DESKTOP",
                                                   "value", "i3"),
                                           Map.of("key", "DISPLAY",
                                                   "value", ":0"),
                                           Map.of("key", "CYGWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "COLORTERM",
                                                   "value", "truecolor"),
                                           Map.of("key", "_",
                                                   "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                        "isAlive", true,
                        "javaCommandLine", "j cl",
                        "jvmPid", 1,
                        "jvmName", "vm",
                        "jvmInfo", "mixed mode",
                        "stopTime", -9223372036854775808L,
                        "startTime", 1495727607481L),
                Map.of("mainClass", "mc",
                        "username", "user",
                        "systemId", "1",
                        "jvmId", "jid2",
                        "javaVersion", "1.8.0_131",
                        "agentId", "aid",
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "vmArguments", "-Djline.log.jul=true",
                        "jvmVersion", "25.131-b12",
                        "lastUpdated", 333,
                        "uid", 1000,
                        "environment", Array.of(
                                           Map.of("key", "PATH",
                                                   "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                           Map.of("key", "XAUTHORITY",
                                                   "value", "/run/user/1000/gdm/Xauthority"),
                                           Map.of("key", "GDMSESSION",
                                                   "value", "i3"),
                                           Map.of("key", "fish_greeting",
                                                   "value", ""),
                                           Map.of("key", "TERM",
                                                   "value", "xterm-256color"),
                                           Map.of("key", "DARWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "LANG",
                                                   "value", "en_US.UTF-8"),
                                           Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                                   "value", "unix:path=/run/user/1000/bus"),
                                           Map.of("key", "XDG_SESSION_ID",
                                                   "value", "2"),
                                           Map.of("key", "XDG_SESSION_TYPE",
                                                   "value", "x11"),
                                           Map.of("key", "XDG_CURRENT_DESKTOP",
                                                   "value", "i3"),
                                           Map.of("key", "DISPLAY",
                                                   "value", ":0"),
                                           Map.of("key", "CYGWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "COLORTERM",
                                                   "value", "truecolor"),
                                           Map.of("key", "_",
                                                   "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                        "isAlive", false,
                        "javaCommandLine", "j cl",
                        "jvmPid", 2,
                        "jvmName", "vm",
                        "jvmInfo", "mixed mode",
                        "stopTime", 1495727607482L,
                        "startTime", 1495727607481L)

        );
        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testGetOffset() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?offset=1";
        ContentResponse getResponse = client.newRequest(url + query).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("environment", Array.of(
                                               Map.of("value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin",
                                                       "key", "PATH"),
                                               Map.of("value", "/run/user/1000/gdm/Xauthority",
                                                       "key", "XAUTHORITY"),
                                               Map.of("value", "i3",
                                                       "key", "GDMSESSION"),
                                               Map.of("value", "",
                                                       "key", "fish_greeting"),
                                               Map.of("value", "xterm-256color",
                                                       "key", "TERM"),
                                               Map.of("value", "0",
                                                       "key", "DARWIN_MODE"),
                                               Map.of("value", "en_US.UTF-8",
                                                       "key", "LANG"),
                                               Map.of("value", "unix:path=/run/user/1000/bus",
                                                       "key", "DBUS_SESSION_BUS_ADDRESS"),
                                               Map.of("value", "2",
                                                       "key", "XDG_SESSION_ID"),
                                               Map.of("value", "x11",
                                                       "key", "XDG_SESSION_TYPE"),
                                               Map.of("value", "i3",
                                                       "key", "XDG_CURRENT_DESKTOP"),
                                               Map.of("value", ":0",
                                                       "key", "DISPLAY"),
                                               Map.of("value", "0",
                                                       "key", "CYGWIN_MODE"),
                                               Map.of("value", "truecolor",
                                                       "key", "COLORTERM"),
                                               Map.of("value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java",
                                                       "key", "_")),
                        "javaCommandLine", "j cl",
                        "jvmName", "vm",
                        "mainClass", "mc",
                        "jvmPid", 2,
                        "lastUpdated", 333,
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "jvmInfo", "mixed mode",
                        "startTime", 1495727607481L,
                        "vmArguments", "-Djline.log.jul=true",
                        "uid", 1000,
                        "jvmId", "jid2",
                        "agentId", "aid",
                        "systemId", "1",
                        "stopTime", 1495727607482L,
                        "isAlive", false,
                        "javaVersion", "1.8.0_131",
                        "username", "user",
                        "jvmVersion", "25.131-b12")

        );

        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testGetQuery() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?query=jvmId==jid2";
        ContentResponse getResponse = client.newRequest(url + query).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("startTime", 1495727607481L,
                        "jvmPid", 2,
                        "javaCommandLine", "j cl",
                        "jvmName", "vm",
                        "lastUpdated", 333,
                        "stopTime", 1495727607482L,
                        "mainClass", "mc",
                        "javaVersion", "1.8.0_131",
                        "jvmVersion", "25.131-b12",
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "jvmInfo", "mixed mode",
                        "username", "user",
                        "isAlive", false,
                        "systemId", "1",
                        "jvmId", "jid2",
                        "uid", 1000,
                        "environment", Array.of(
                                           Map.of("key", "PATH",
                                               "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                           Map.of("key", "XAUTHORITY",
                                                   "value", "/run/user/1000/gdm/Xauthority"),
                                           Map.of("key", "GDMSESSION",
                                                   "value", "i3"),
                                           Map.of("key", "fish_greeting",
                                                   "value", ""),
                                           Map.of("key", "TERM",
                                                   "value", "xterm-256color"),
                                           Map.of("key", "DARWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "LANG",
                                                   "value", "en_US.UTF-8"),
                                           Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                                   "value", "unix:path=/run/user/1000/bus"),
                                           Map.of("key", "XDG_SESSION_ID",
                                                   "value", "2"),
                                           Map.of("key", "XDG_SESSION_TYPE",
                                                   "value", "x11"),
                                           Map.of("key", "XDG_CURRENT_DESKTOP",
                                                   "value", "i3"),
                                           Map.of("key", "DISPLAY",
                                                   "value", ":0"),
                                           Map.of("key", "CYGWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "COLORTERM",
                                                   "value", "truecolor"),
                                           Map.of("key", "_",
                                                   "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                        "vmArguments", "-Djline.log.jul=true",
                        "agentId", "aid")
        );

        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testGetSysJvm() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "/jvms/jid2";
        ContentResponse getResponse = client.newRequest(url + query).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "agentId", "aid",
                        "startTime", 1495727607481L,
                        "mainClass", "mc",
                        "vmArguments", "-Djline.log.jul=true",
                        "javaVersion", "1.8.0_131",
                        "jvmId", "jid2",
                        "systemId", "1",
                        "jvmInfo", "mixed mode",
                        "environment", Array.of(
                                           Map.of("key", "PATH",
                                                   "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                           Map.of("key", "XAUTHORITY",
                                                   "value", "/run/user/1000/gdm/Xauthority"),
                                           Map.of("key", "GDMSESSION",
                                                   "value", "i3"),
                                           Map.of("key", "fish_greeting",
                                                   "value", ""),
                                           Map.of("key", "TERM",
                                                   "value", "xterm-256color"),
                                           Map.of("key", "DARWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "LANG",
                                                   "value", "en_US.UTF-8"),
                                           Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                                   "value", "unix:path=/run/user/1000/bus"),
                                           Map.of("key", "XDG_SESSION_ID",
                                                   "value", "2"),
                                           Map.of("key", "XDG_SESSION_TYPE",
                                                   "value", "x11"),
                                           Map.of("key", "XDG_CURRENT_DESKTOP",
                                                   "value", "i3"),
                                           Map.of("key", "DISPLAY",
                                                   "value", ":0"),
                                           Map.of("key", "CYGWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "COLORTERM",
                                                   "value", "truecolor"),
                                           Map.of("key", "_",
                                                   "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                        "jvmName", "vm",
                        "javaCommandLine", "j cl",
                        "isAlive", false,
                        "jvmVersion", "25.131-b12",
                        "jvmPid", 2,
                        "username", "user",
                        "stopTime", 1495727607482L,
                        "lastUpdated", 333,
                        "uid", 1000)
        );

        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testGetInclude() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?include=agentId,jvmId";
        ContentResponse getResponse = client.newRequest(url + query).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("agentId", "aid", "jvmId", "jid1", "isAlive", true)
        );
        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testGetExclude() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?exclude=agentId";
        ContentResponse getResponse = client.newRequest(url + query).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("jvmPid", 1,
                        "javaVersion", "1.8.0_131",
                        "environment", Array.of(
                                           Map.of("value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin",
                                               "key", "PATH"),
                                           Map.of("value", "/run/user/1000/gdm/Xauthority",
                                                   "key", "XAUTHORITY"),
                                           Map.of("value", "i3",
                                                   "key", "GDMSESSION"),
                                           Map.of("value", "",
                                                   "key", "fish_greeting"),
                                           Map.of("value", "xterm-256color",
                                                   "key", "TERM"),
                                           Map.of("value", "0",
                                                   "key", "DARWIN_MODE"),
                                           Map.of("value", "en_US.UTF-8",
                                                   "key", "LANG"),
                                           Map.of("value", "unix:path=/run/user/1000/bus",
                                                   "key", "DBUS_SESSION_BUS_ADDRESS"),
                                           Map.of("value", "2",
                                                   "key", "XDG_SESSION_ID"),
                                           Map.of("value", "x11",
                                                   "key", "XDG_SESSION_TYPE"),
                                           Map.of("value", "i3",
                                                   "key", "XDG_CURRENT_DESKTOP"),
                                           Map.of("value", ":0",
                                                   "key", "DISPLAY"),
                                           Map.of("value", "0",
                                                   "key", "CYGWIN_MODE"),
                                           Map.of("value", "truecolor",
                                                   "key", "COLORTERM"),
                                           Map.of("value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java",
                                                   "key", "_")),
                        "isAlive", true,
                        "uid", 1000,
                        "systemId", "1",
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "jvmVersion", "25.131-b12",
                        "stopTime", -9223372036854775808L,
                        "jvmId", "jid1",
                        "jvmName", "vm",
                        "jvmInfo", "mixed mode",
                        "vmArguments", "-Djline.log.jul=true",
                        "mainClass", "mc",
                        "startTime", 1495727607481L,
                        "lastUpdated", 333,
                        "username", "user",
                        "javaCommandLine", "j cl")
        );

        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testDeleteMany() throws InterruptedException, TimeoutException, ExecutionException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ContentResponse deleteResponse = client.newRequest(url).method(HttpMethod.DELETE).send();
        assertEquals(200, deleteResponse.getStatus());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());
        String expected = HttpTestUtil.EMPTY_RESPONSE;
        assertEquals(expected, getResponse.getContentAsString());
    }

    @Test
    public void testPut() throws InterruptedException, TimeoutException, ExecutionException {
        String url = jvmsUrl + "/systems/1";
        String putUrl = jvmsUrl + "/systems/1/jvms/jid1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ResponseSetWrapper responseSetWrapper = new ResponseSetWrapper().key("mainClass").value("hello")
                                                                       .key("javaVersion").value("1.7.0");
        ContentResponse putResponse = client.newRequest(putUrl).method(HttpMethod.PUT).
                content(new StringContentProvider(responseSetWrapper.toSetString()), "application/json").send();
        assertEquals(200, putResponse.getStatus());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("jvmName", "vm",
                        "username", "user",
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "javaCommandLine", "j cl",
                        "agentId", "aid",
                        "jvmId", "jid1",
                        "environment", Array.of(
                                           Map.of("key", "PATH",
                                               "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                           Map.of("key", "XAUTHORITY",
                                                   "value", "/run/user/1000/gdm/Xauthority"),
                                           Map.of("key", "GDMSESSION",
                                                   "value", "i3"),
                                           Map.of("key", "fish_greeting",
                                                   "value", ""),
                                           Map.of("key", "TERM",
                                                   "value", "xterm-256color"),
                                           Map.of("key", "DARWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "LANG",
                                                   "value", "en_US.UTF-8"),
                                           Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                                   "value", "unix:path=/run/user/1000/bus"),
                                           Map.of("key", "XDG_SESSION_ID",
                                                   "value", "2"),
                                           Map.of("key", "XDG_SESSION_TYPE",
                                                   "value", "x11"),
                                           Map.of("key", "XDG_CURRENT_DESKTOP",
                                                   "value", "i3"),
                                           Map.of("key", "DISPLAY",
                                                   "value", ":0"),
                                           Map.of("key", "CYGWIN_MODE",
                                                   "value", "0"),
                                           Map.of("key", "COLORTERM",
                                                   "value", "truecolor"),
                                           Map.of("key", "_",
                                                   "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                        "mainClass", "hello",
                        "uid", 1000,
                        "systemId", "1",
                        "stopTime", -9223372036854775808L,
                        "jvmInfo", "mixed mode",
                        "jvmVersion", "25.131-b12",
                        "lastUpdated", 333,
                        "startTime", 1495727607481L,
                        "jvmPid", 1,
                        "vmArguments", "-Djline.log.jul=true",
                        "javaVersion", "1.7.0",
                        "isAlive", true)
        );

        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    /*
     * Verify that PUT cannot be used to update systemId or jvmId fields
     */
    @Test
    public void testPutInvalidField() throws InterruptedException, TimeoutException, ExecutionException {
        String url = jvmsUrl + "/systems/1";
        String putUrl = jvmsUrl + "/systems/1/jvms/jid1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ResponseSetWrapper responseSetWrapper = new ResponseSetWrapper().key("systemId").value("hello");
        ContentResponse putResponse = client.newRequest(putUrl).method(HttpMethod.PUT)
                .content(new StringContentProvider(responseSetWrapper.toSetString()), "application/json").send();
        assertEquals(400, putResponse.getStatus());

        responseSetWrapper = new ResponseSetWrapper().key("jvmId").value("hello");
        putResponse = client.newRequest(putUrl).method(HttpMethod.PUT)
                .content(new StringContentProvider(responseSetWrapper.toSetString()), "application/json").send();
        assertEquals(400, putResponse.getStatus());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("jvmId", "jid1",
                        "environment", Array.of(
                                           Map.of("value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin",
                                               "key", "PATH"),
                                           Map.of("value", "/run/user/1000/gdm/Xauthority",
                                                   "key", "XAUTHORITY"),
                                           Map.of("value", "i3",
                                                   "key", "GDMSESSION"),
                                           Map.of("value", "",
                                                   "key", "fish_greeting"),
                                           Map.of("value", "xterm-256color",
                                                   "key", "TERM"),
                                           Map.of("value", "0",
                                                   "key", "DARWIN_MODE"),
                                           Map.of("value", "en_US.UTF-8",
                                                   "key", "LANG"),
                                           Map.of("value", "unix:path=/run/user/1000/bus",
                                                   "key", "DBUS_SESSION_BUS_ADDRESS"),
                                           Map.of("value", "2",
                                                   "key", "XDG_SESSION_ID"),
                                           Map.of("value", "x11",
                                                   "key", "XDG_SESSION_TYPE"),
                                           Map.of("value", "i3",
                                                   "key", "XDG_CURRENT_DESKTOP"),
                                           Map.of("value", ":0",
                                                   "key", "DISPLAY"),
                                           Map.of("value", "0",
                                                   "key", "CYGWIN_MODE"),
                                           Map.of("value", "truecolor",
                                                   "key", "COLORTERM"),
                                           Map.of("value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java",
                                                   "key", "_")),
                        "stopTime", -9223372036854775808L,
                        "lastUpdated", 333,
                        "systemId", "1",
                        "uid", 1000,
                        "username", "user",
                        "vmArguments", "-Djline.log.jul=true",
                        "mainClass", "mc",
                        "jvmPid", 1,
                        "jvmVersion", "25.131-b12",
                        "jvmName", "vm",
                        "javaCommandLine", "j cl",
                        "isAlive", true,
                        "javaVersion", "1.8.0_131",
                        "jvmInfo", "mixed mode",
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "startTime", 1495727607481L,
                        "agentId", "aid")
        );

        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testDeleteOne() throws InterruptedException, TimeoutException, ExecutionException {
        String url = jvmsUrl + "/systems/1";
        String deleteUrl = jvmsUrl + "/systems/1/jvms/jid1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ContentResponse deleteResponse = client.newRequest(deleteUrl).method(HttpMethod.DELETE).send();
        assertEquals(200, deleteResponse.getStatus());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("environment", Array.of(
                                                   Map.of("value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin",
                                                       "key", "PATH"),
                                                   Map.of("value", "/run/user/1000/gdm/Xauthority",
                                                           "key", "XAUTHORITY"),
                                                   Map.of("value", "i3",
                                                           "key", "GDMSESSION"),
                                                   Map.of("value", "",
                                                           "key", "fish_greeting"),
                                                   Map.of("value", "xterm-256color",
                                                           "key", "TERM"),
                                                   Map.of("value", "0",
                                                           "key", "DARWIN_MODE"),
                                                   Map.of("value", "en_US.UTF-8",
                                                           "key", "LANG"),
                                                   Map.of("value", "unix:path=/run/user/1000/bus",
                                                           "key", "DBUS_SESSION_BUS_ADDRESS"),
                                                   Map.of("value", "2",
                                                           "key", "XDG_SESSION_ID"),
                                                   Map.of("value", "x11",
                                                           "key", "XDG_SESSION_TYPE"),
                                                   Map.of("value", "i3",
                                                           "key", "XDG_CURRENT_DESKTOP"),
                                                   Map.of("value", ":0",
                                                           "key", "DISPLAY"),
                                                   Map.of("value", "0",
                                                           "key", "CYGWIN_MODE"),
                                                   Map.of("value", "truecolor",
                                                           "key", "COLORTERM"),
                                                   Map.of("value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java",
                                                           "key", "_")),
                        "systemId", "1",
                        "uid", 1000,
                        "username", "user",
                        "javaCommandLine", "j cl",
                        "isAlive", false,
                        "startTime", 1495727607481L,
                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                        "stopTime", 1495727607482L,
                        "mainClass", "mc",
                        "jvmId", "jid2",
                        "agentId", "aid",
                        "javaVersion", "1.8.0_131",
                        "lastUpdated", 333,
                        "jvmPid", 2,
                        "jvmName", "vm",
                        "vmArguments", "-Djline.log.jul=true",
                        "jvmVersion", "25.131-b12",
                        "jvmInfo", "mixed mode")

        );

        expectedContentWrapper.matchJsonOrThrow(getResponse.getContentAsString());
    }

    @Test
    public void testUpdateTimestamp() throws InterruptedException, TimeoutException, ExecutionException {
        String updateUrl = jvmsUrl + "/update/systems/1/ts/2000";
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ContentResponse updateResponse = client.newRequest(updateUrl).method(HttpMethod.PUT).send();
        assertEquals(200, updateResponse.getStatus());

        ContentResponse response = client.newRequest(url).method(HttpMethod.GET)
                .param("include", "lastUpdated").send();
        assertEquals(200, response.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("lastUpdated", 2000, "isAlive", true));
        expectedContentWrapper.matchJsonOrThrow(response.getContentAsString());
    }

    @Test
    public void testTree() throws InterruptedException, TimeoutException, ExecutionException {
        String postUrl = jvmsUrl + "/systems/1";
        String treeUrl = jvmsUrl + "/tree";

        ContentResponse postResponse = client.newRequest(postUrl).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ContentResponse response = client.newRequest(treeUrl).method(HttpMethod.GET).send();
        assertEquals(200, response.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("systemId", "1",
                        "jvms", Array.of(
                                        Map.of("environment", Array.of(Map.of("value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin",
                                               "key", "PATH"),
                                        Map.of("value", "/run/user/1000/gdm/Xauthority",
                                               "key", "XAUTHORITY"),
                                        Map.of("value", "i3",
                                               "key", "GDMSESSION"),
                                        Map.of("value", "",
                                               "key", "fish_greeting"),
                                        Map.of("value", "xterm-256color",
                                               "key", "TERM"),
                                        Map.of("value", "0",
                                               "key", "DARWIN_MODE"),
                                        Map.of("value", "en_US.UTF-8",
                                               "key", "LANG"),
                                        Map.of("value", "unix:path=/run/user/1000/bus",
                                               "key", "DBUS_SESSION_BUS_ADDRESS"),
                                        Map.of("value", "2",
                                               "key", "XDG_SESSION_ID"),
                                        Map.of("value", "x11",
                                               "key", "XDG_SESSION_TYPE"),
                                        Map.of("value", "i3",
                                               "key", "XDG_CURRENT_DESKTOP"),
                                        Map.of("value", ":0",
                                               "key", "DISPLAY"),
                                        Map.of("value", "0",
                                               "key", "CYGWIN_MODE"),
                                        Map.of("value", "truecolor",
                                               "key", "COLORTERM"),
                                        Map.of("value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java",
                                               "key", "_")),
                                "jvmName", "vm",
                                "stopTime", Map.of("$numberLong", "-9223372036854775808"),
                                "startTime", Map.of("$numberLong", "1495727607481"),
                                "javaVersion", "1.8.0_131",
                                "lastUpdated", Map.of("$numberLong", "333"),
                                "javaCommandLine", "j cl",
                                "username", "user",
                                "vmArguments", "-Djline.log.jul=true",
                                "mainClass", "mc",
                                "agentId", "aid",
                                "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                                "jvmVersion", "25.131-b12",
                                "isAlive", true,
                                "systemId", "1",
                                "jvmId", "jid1",
                                "jvmInfo", "mixed mode",
                                "jvmPid", 1,
                                "uid", 1000)))
        );

        expectedContentWrapper.matchJsonOrThrow(response.getContentAsString());
    }

    @Test
    public void testTreeInclude() throws InterruptedException, TimeoutException, ExecutionException {
        String postUrl = jvmsUrl + "/systems/1";
        String treeUrl = jvmsUrl + "/tree";

        ContentResponse postResponse = client.newRequest(postUrl).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?include=jvmId";
        ContentResponse response = client.newRequest(treeUrl + query).method(HttpMethod.GET).send();
        assertEquals(200, response.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("jvms", Array.of(
                        Map.of("jvmId", "jid1",
                                "isAlive", true)),
                                "systemId", "1")
        );
        expectedContentWrapper.matchJsonOrThrow(response.getContentAsString());
    }

    @Test
    public void testTreeExclude() throws InterruptedException, TimeoutException, ExecutionException {
        String postUrl = jvmsUrl + "/systems/1";
        String treeUrl = jvmsUrl + "/tree";

        ContentResponse postResponse = client.newRequest(postUrl).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?exclude=systemId";
        ContentResponse response = client.newRequest(treeUrl + query).method(HttpMethod.GET).send();
        assertEquals(200, response.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("systemId", "1",
                        "jvms", Array.of(Map.of("jvmId", "jid1",
                                "jvmName", "vm",
                                "username", "user",
                                "startTime", Map.of("$numberLong", "1495727607481"),
                                "mainClass", "mc",
                                "uid", 1000,
                                "isAlive", true,
                                "vmArguments", "-Djline.log.jul=true",
                                "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                                "javaCommandLine", "j cl",
                                "environment", Array.of(
                                                   Map.of("value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin",
                                                           "key", "PATH"),
                                                   Map.of("value", "/run/user/1000/gdm/Xauthority",
                                                           "key", "XAUTHORITY"),
                                                   Map.of("value", "i3",
                                                           "key", "GDMSESSION"),
                                                   Map.of("value", "",
                                                           "key", "fish_greeting"),
                                                   Map.of("value", "xterm-256color",
                                                           "key", "TERM"),
                                                   Map.of("value", "0",
                                                           "key", "DARWIN_MODE"),
                                                   Map.of("value", "en_US.UTF-8",
                                                           "key", "LANG"),
                                                   Map.of("value", "unix:path=/run/user/1000/bus",
                                                           "key", "DBUS_SESSION_BUS_ADDRESS"),
                                                   Map.of("value", "2",
                                                           "key", "XDG_SESSION_ID"),
                                                   Map.of("value", "x11",
                                                           "key", "XDG_SESSION_TYPE"),
                                                   Map.of("value", "i3",
                                                           "key", "XDG_CURRENT_DESKTOP"),
                                                   Map.of("value", ":0",
                                                           "key", "DISPLAY"),
                                                   Map.of("value", "0",
                                                           "key", "CYGWIN_MODE"),
                                                   Map.of("value", "truecolor",
                                                           "key", "COLORTERM"),
                                                   Map.of("value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java",
                                                           "key", "_")),
                                "javaVersion", "1.8.0_131",
                                "jvmInfo", "mixed mode",
                                "jvmPid", 1,
                                "stopTime", Map.of("$numberLong", "-9223372036854775808"),
                                "jvmVersion", "25.131-b12",
                                "lastUpdated", Map.of("$numberLong", "333"),
                                "agentId", "aid")))
        );

        expectedContentWrapper.matchJsonOrThrow(response.getContentAsString());
    }

    @Test
    public void testTreeAllLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String postUrl = jvmsUrl + "/systems/1";
        String treeUrl = jvmsUrl + "/tree";

        ContentResponse postResponse = client.newRequest(postUrl).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?aliveOnly=false&limit=2";
        ContentResponse response = client.newRequest(treeUrl + query).method(HttpMethod.GET).send();
        assertEquals(200, response.getStatus());

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("systemId", "1",
                        "jvms", Array.of(Map.of("mainClass", "mc",
                                "uid", 1000,
                                "agentId", "aid",
                                "environment", Array.of(
                                                   Map.of("key", "PATH",
                                                       "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                                   Map.of("key", "XAUTHORITY",
                                                           "value", "/run/user/1000/gdm/Xauthority"),
                                                   Map.of("key", "GDMSESSION",
                                                           "value", "i3"),
                                                   Map.of("key", "fish_greeting",
                                                           "value", ""),
                                                   Map.of("key", "TERM",
                                                           "value", "xterm-256color"),
                                                   Map.of("key", "DARWIN_MODE",
                                                           "value", "0"),
                                                   Map.of("key", "LANG",
                                                           "value", "en_US.UTF-8"),
                                                   Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                                           "value", "unix:path=/run/user/1000/bus"),
                                                   Map.of("key", "XDG_SESSION_ID",
                                                           "value", "2"),
                                                   Map.of("key", "XDG_SESSION_TYPE",
                                                           "value", "x11"),
                                                   Map.of("key", "XDG_CURRENT_DESKTOP",
                                                           "value", "i3"),
                                                   Map.of("key", "DISPLAY",
                                                           "value", ":0"),
                                                   Map.of("key", "CYGWIN_MODE",
                                                           "value", "0"),
                                                   Map.of("key", "COLORTERM",
                                                           "value", "truecolor"),
                                                   Map.of("key", "_",
                                                           "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                                "jvmId", "jid1",
                                "javaVersion", "1.8.0_131",
                                "javaCommandLine", "j cl",
                                "jvmPid", 1,
                                "jvmInfo", "mixed mode",
                                "systemId", "1",
                                "isAlive", true,
                                "jvmName", "vm",
                                "startTime", Map.of("$numberLong", "1495727607481"),
                                "stopTime", Map.of("$numberLong", "-9223372036854775808"),
                                "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                                "vmArguments", "-Djline.log.jul=true",
                                "jvmVersion", "25.131-b12",
                                "lastUpdated", Map.of("$numberLong", "333"),
                                "username", "user"),
                                Map.of("mainClass", "mc",
                                        "uid", 1000,
                                        "agentId", "aid",
                                        "environment", Array.of(
                                                           Map.of("key", "PATH",
                                                               "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                                           Map.of("key", "XAUTHORITY",
                                                                   "value", "/run/user/1000/gdm/Xauthority"),
                                                           Map.of("key", "GDMSESSION",
                                                                   "value", "i3"),
                                                           Map.of("key", "fish_greeting",
                                                                   "value", ""),
                                                           Map.of("key", "TERM",
                                                                   "value", "xterm-256color"),
                                                           Map.of("key", "DARWIN_MODE",
                                                                   "value", "0"),
                                                           Map.of("key", "LANG",
                                                                   "value", "en_US.UTF-8"),
                                                           Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                                                   "value", "unix:path=/run/user/1000/bus"),
                                                           Map.of("key", "XDG_SESSION_ID",
                                                                   "value", "2"),
                                                           Map.of("key", "XDG_SESSION_TYPE",
                                                                   "value", "x11"),
                                                           Map.of("key", "XDG_CURRENT_DESKTOP",
                                                                   "value", "i3"),
                                                           Map.of("key", "DISPLAY",
                                                                   "value", ":0"),
                                                           Map.of("key", "CYGWIN_MODE",
                                                                   "value", "0"),
                                                           Map.of("key", "COLORTERM",
                                                                   "value", "truecolor"),
                                                           Map.of("key", "_",
                                                                   "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                                        "jvmId", "jid2",
                                        "javaVersion", "1.8.0_131",
                                        "javaCommandLine", "j cl",
                                        "jvmPid", 2,
                                        "jvmInfo", "mixed mode",
                                        "systemId", "1",
                                        "isAlive", false,
                                        "jvmName", "vm",
                                        "startTime", Map.of("$numberLong", "1495727607481"),
                                        "stopTime", Map.of("$numberLong", "1495727607482"),
                                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                                        "vmArguments", "-Djline.log.jul=true",
                                        "jvmVersion", "25.131-b12",
                                        "lastUpdated", Map.of("$numberLong", "333"),
                                        "username", "user")))
        );

        expectedContentWrapper.matchJsonOrThrow(response.getContentAsString());
    }

    @Test
    public void testTreeVersions() throws InterruptedException, TimeoutException, ExecutionException {
        String postUrl = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(postUrl).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?aliveOnly=false&offset=1";

        ContentWrapper expectedContentWrapper = new ContentWrapper().addToResponse(
                Map.of("jvms",
                        Array.of(
                                Map.of("jvmName", "vm",
                                        "lastUpdated", Map.of("$numberLong", "333"),
                                        "vmArguments", "-Djline.log.jul=true",
                                        "mainClass", "mc",
                                        "javaVersion", "1.8.0_131",
                                        "stopTime", Map.of("$numberLong", "1495727607482"),
                                        "jvmVersion", "25.131-b12",
                                        "jvmId", "jid2",
                                        "username", "user",
                                        "isAlive", false,
                                        "jvmPid", 2,
                                        "javaCommandLine", "j cl",
                                        "jvmInfo", "mixed mode",
                                        "javaHome", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre",
                                        "startTime", Map.of("$numberLong", "1495727607481"),
                                        "environment", Array.of(
                                                Map.of("key", "PATH",
                                                        "value", "/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin"),
                                                Map.of("key", "XAUTHORITY",
                                                        "value", "/run/user/1000/gdm/Xauthority"),
                                                Map.of("key", "GDMSESSION",
                                                        "value", "i3"),
                                                Map.of("key", "fish_greeting",
                                                        "value", ""),
                                                Map.of("key", "TERM",
                                                        "value", "xterm-256color"),
                                                Map.of("key", "DARWIN_MODE",
                                                        "value", "0"),
                                                Map.of("key", "LANG",
                                                        "value", "en_US.UTF-8"),
                                                Map.of("key", "DBUS_SESSION_BUS_ADDRESS",
                                                        "value", "unix:path=/run/user/1000/bus"),
                                                Map.of("key", "XDG_SESSION_ID",
                                                        "value", "2"),
                                                Map.of("key", "XDG_SESSION_TYPE",
                                                        "value", "x11"),
                                                Map.of("key", "XDG_CURRENT_DESKTOP",
                                                        "value", "i3"),
                                                Map.of("key", "DISPLAY",
                                                        "value", ":0"),
                                                Map.of("key", "CYGWIN_MODE",
                                                        "value", "0"),
                                                Map.of("key", "COLORTERM",
                                                        "value", "truecolor"),
                                                Map.of("key", "_",
                                                        "value", "/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java")),
                                        "uid", 1000,
                                        "agentId", "aid",
                                        "systemId", "1")),
                        "systemId", "1")
        );

        final String url1 = baseUrl + "/jvms/0.0/tree";
        ContentResponse response = client.newRequest(url1 + query).method(HttpMethod.GET).send();
        assertEquals(200, response.getStatus());
        expectedContentWrapper.matchJsonOrThrow(response.getContentAsString());

        final String url2 = baseUrl + "/jvms/0.0.0/tree";
        response = client.newRequest(url2 + query).method(HttpMethod.GET).send();
        assertEquals(200, response.getStatus());
        expectedContentWrapper.matchJsonOrThrow(response.getContentAsString());

        final String url3 = baseUrl + "/jvms/0.0.5/tree";
        response = client.newRequest(url3 + query).method(HttpMethod.GET).send();
        assertEquals(404, response.getStatus());
    }
}
