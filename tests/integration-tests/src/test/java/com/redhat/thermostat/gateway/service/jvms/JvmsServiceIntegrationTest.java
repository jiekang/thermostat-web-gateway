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

import com.redhat.thermostat.gateway.tests.integration.MongoIntegrationTest;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

public class JvmsServiceIntegrationTest extends MongoIntegrationTest {
    private static final String collectionName = "jvm-info";
    private static final String jvmsUrl = baseUrl + "/jvms/0.0.1";

    private final String postData = "[{ \"agentId\" : \"aid\", \"jvmId\" : \"jid1\", \"jvmPid\" : 1, \"startTime\" :" +
            "{ \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" : \"-9223372036854775808\" }," +
            " \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\"," +
            " \"mainClass\" : \"mc\", \"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" :" +
            " \"-Djline.log.jul=true\", \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" :" +
            " \"25.131-b12\", \"environment\" : [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\"" +
            " }, { \"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\"," +
            " \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\", \"value\" :" +
            " \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" : \"LANG\", \"value\" :" +
            " \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" : \"unix:path=/run/user/1000/bus\" }," +
            " { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\" : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }," +
            " { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" : \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }," +
            " { \"key\" : \"CYGWIN_MODE\", \"value\" : \"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, " +
            "{ \"key\" : \"_\", \"value\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }]," +
            " \"uid\" : 1000, \"username\" : \"user\"},{ \"agentId\" : \"aid\", \"jvmId\" : \"jid2\", \"jvmPid\" : 2," +
            " \"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" : \"1495727607482\" }," +
            " \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\"," +
            " \"mainClass\" : \"mc\", \"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" :" +
            " \"-Djline.log.jul=true\", \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\"," +
            " \"environment\" : [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }," +
            " { \"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\"," +
            " \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\", \"value\" :" +
            " \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" : \"LANG\", \"value\" :" +
            " \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" : \"unix:path=/run/user/1000/bus\" }," +
            " { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\" : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }," +
            " { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" : \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }," +
            " { \"key\" : \"CYGWIN_MODE\", \"value\" : \"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }," +
            " { \"key\" : \"_\", \"value\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }]," +
            " \"uid\" : 1000, \"username\" : \"user\"}]";

        private final String postDataWithSystemId = postData.substring(0, postData.length() - 2) +
                ", \"systemId\" : \"invalid\"}]";

    public JvmsServiceIntegrationTest() {
        super(jvmsUrl, "jvm-info");
    }

    @Test
    public void testGetEmpty() throws InterruptedException, TimeoutException, ExecutionException {
        String url = jvmsUrl + "/systems/1";
        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, response.getStatus());
        String expected = "{ \"response\" : [] }";
        assertEquals(expected, response.getContentAsString());
    }

    @Test
    public void testPost() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());
        String expected = "{ \"response\" : [{ \"agentId\" : \"aid\", \"jvmId\" : \"jid1\", \"jvmPid\" : 1, " +
                "\"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" : " +
                "\"-9223372036854775808\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : " +
                "\"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\", " +
                "\"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\"," +
                " \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\"" +
                " : [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }," +
                " { \"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" :" +
                " \"GDMSESSION\", \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" :" +
                " \"TERM\", \"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }," +
                " { \"key\" : \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\"," +
                " \"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }," +
                " { \"key\" : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\"," +
                " \"value\" : \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\"," +
                " \"value\" : \"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", " +
                "\"value\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], " +
                "\"uid\" : 1000, \"username\" : \"user\", \"systemId\" : \"1\" }] }";
        assertEquals(expected, getResponse.getContentAsString());
    }

    /*
     * Verify POST does not override path parameter systemId with body systemId
     * POST /systems/1 [{"systemId":2 ... }] should result in systemId of 1, not 2
     */
    @Test
    public void testPostWithSystemId() throws InterruptedException, ExecutionException, TimeoutException {
        String url = jvmsUrl + "/systems/1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postDataWithSystemId), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());
        String expected = "{ \"response\" : [{ \"agentId\" : \"aid\", \"jvmId\" : \"jid1\", \"jvmPid\" : 1," +
                " \"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" :" +
                " \"-9223372036854775808\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\", " +
                "\"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\"," +
                " \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\"" +
                " : [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, { " +
                "\"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\"," +
                " \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\", " +
                "\"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" : " +
                "\"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\"" +
                " : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" : " +
                "\"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\", \"value\" :" +
                " \"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", \"value\"" +
                " : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], \"uid\" : 1000," +
                " \"username\" : \"user\", \"systemId\" : \"1\" }] }";
        assertEquals(expected, getResponse.getContentAsString());
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
        String expected = "{ \"response\" : [{ \"agentId\" : \"aid\", \"jvmId\" : \"jid1\", \"jvmPid\" : 1," +
                " \"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" :" +
                " \"-9223372036854775808\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : " +
                "\"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\"," +
                " \"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\"," +
                " \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\" " +
                ": [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, {" +
                " \"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : " +
                "\"GDMSESSION\", \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" :" +
                " \"TERM\", \"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { " +
                "\"key\" : \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\"" +
                " : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { " +
                "\"key\" : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\"" +
                " : \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\", \"value\" :" +
                " \"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", \"value\" : " +
                "\"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], \"uid\" : 1000, " +
                "\"username\" : \"user\", \"systemId\" : \"1\" },{ \"agentId\" : \"aid\", \"jvmId\" : \"jid2\", " +
                "\"jvmPid\" : 2, \"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { " +
                "\"$numberLong\" : \"1495727607482\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : " +
                "\"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\", " +
                "\"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\", " +
                "\"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\" :" +
                " [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, { " +
                "\"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\"," +
                " \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\"," +
                " \"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" :" +
                " \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" : " +
                "\"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\" : " +
                "\"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" : \"i3\"" +
                " }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\", \"value\" : \"0\"" +
                " }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", \"value\" : " +
                "\"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], \"uid\" : 1000, " +
                "\"username\" : \"user\", \"systemId\" : \"1\" }] }";
        assertEquals(expected, getResponse.getContentAsString());
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
        String expected = "{ \"response\" : [{ \"agentId\" : \"aid\", \"jvmId\" : \"jid2\", \"jvmPid\" : 2, " +
                "\"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" : " +
                "\"1495727607482\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\"," +
                " \"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\"," +
                " \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\" " +
                ": [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, { " +
                "\"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\"," +
                " \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\"," +
                " \"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" :" +
                " \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\" " +
                ": \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" :" +
                " \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\", \"value\" : " +
                "\"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", \"value\" :" +
                " \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], \"uid\" : 1000," +
                " \"username\" : \"user\", \"systemId\" : \"1\" }] }";
        assertEquals(expected, getResponse.getContentAsString());
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
        String expected = "{ \"response\" : [{ \"agentId\" : \"aid\", \"jvmId\" : \"jid2\", \"jvmPid\" : 2, " +
                "\"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" : " +
                "\"1495727607482\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\"," +
                " \"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\"," +
                " \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\" " +
                ": [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, { " +
                "\"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\"," +
                " \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\"," +
                " \"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" :" +
                " \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\" " +
                ": \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" :" +
                " \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\", \"value\" : " +
                "\"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", \"value\" :" +
                " \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], \"uid\" : 1000," +
                " \"username\" : \"user\", \"systemId\" : \"1\" }] }";
        assertEquals(expected, getResponse.getContentAsString());
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
        String expected = "{ \"response\" : [{ \"agentId\" : \"aid\", \"jvmId\" : \"jid1\" }] }";
        assertEquals(expected, getResponse.getContentAsString());
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
        String expected = "{ \"response\" : [{ \"jvmId\" : \"jid1\", \"jvmPid\" : 1, \"startTime\" : { \"$numberLong\"" +
                " : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" : \"-9223372036854775808\" }, \"javaVersion\"" +
                " : \"1.8.0_131\", \"javaHome\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\"," +
                " \"mainClass\" : \"mc\", \"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" :" +
                " \"-Djline.log.jul=true\", \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : " +
                "\"25.131-b12\", \"environment\" : [{ \"key\" : \"PATH\", \"value\" : " +
                "\"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, { \"key\" : \"XAUTHORITY\", \"value\" : " +
                "\"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\", \"value\" : \"i3\" }, { \"key\" :" +
                " \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\", \"value\" : \"xterm-256color\" }, { " +
                "\"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" : \"LANG\", \"value\" : \"en_US.UTF-8\" }, {" +
                " \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" :" +
                " \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\" : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }," +
                " { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" : \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" :" +
                " \":0\" }, { \"key\" : \"CYGWIN_MODE\", \"value\" : \"0\" }, { \"key\" : \"COLORTERM\", \"value\" :" +
                " \"truecolor\" }, { \"key\" : \"_\", \"value\" : " +
                "\"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], \"uid\" : 1000, " +
                "\"username\" : \"user\", \"systemId\" : \"1\" }] }";
        assertEquals(expected, getResponse.getContentAsString());
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
        String expected = "{ \"response\" : [] }";
        assertEquals(expected, getResponse.getContentAsString());
    }

    @Test
    public void testPut() throws InterruptedException, TimeoutException, ExecutionException {
        String url = jvmsUrl + "/systems/1";
        String putUrl = jvmsUrl + "/systems/1/jvms/jid1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String setContent = "{\"set\":{\"mainClass\":\"hello\",\"javaVersion\":\"1.7.0\"}}";
        ContentResponse putResponse = client.newRequest(putUrl).method(HttpMethod.PUT).
                content(new StringContentProvider(setContent), "application/json").send();
        assertEquals(200, putResponse.getStatus());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());
        String expected = "{ \"response\" : [{ \"agentId\" : \"aid\", \"jvmId\" : \"jid1\", \"jvmPid\" : 1, " +
                "\"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" : " +
                "\"-9223372036854775808\" }, \"javaVersion\" : \"1.7.0\", \"javaHome\" : " +
                "\"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"hello\", " +
                "\"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\"," +
                " \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\"" +
                " : [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, " +
                "{ \"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : " +
                "\"GDMSESSION\", \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : " +
                "\"TERM\", \"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { " +
                "\"key\" : \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", " +
                "\"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, " +
                "{ \"key\" : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", " +
                "\"value\" : \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\"," +
                " \"value\" : \"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", " +
                "\"value\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], " +
                "\"uid\" : 1000, \"username\" : \"user\", \"systemId\" : \"1\" }] }";
        assertEquals(expected, getResponse.getContentAsString());
    }

    /*
     * Verify that PUT cannot be used to update systemId or jvmId fields
     */
    @Test
    public void testPutInvalidField() throws InterruptedException, TimeoutException, ExecutionException {
        //
        String url = jvmsUrl + "/systems/1";
        String putUrl = jvmsUrl + "/systems/1/jvms/jid1";

        ContentResponse postResponse = client.newRequest(url).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String setContent = "{\"set\":{\"systemId\":\"hello\"}}";
        ContentResponse putResponse = client.newRequest(putUrl).method(HttpMethod.PUT)
                .content(new StringContentProvider(setContent), "application/json").send();
        assertEquals(400, putResponse.getStatus());

        setContent = "{\"set\":{\"jvmId\":\"hello\"}}";
        putResponse = client.newRequest(putUrl).method(HttpMethod.PUT)
                .content(new StringContentProvider(setContent), "application/json").send();
        assertEquals(400, putResponse.getStatus());

        ContentResponse getResponse = client.newRequest(url).method(HttpMethod.GET).send();
        assertEquals(200, getResponse.getStatus());
        String expected = "{ \"response\" : [{ \"agentId\" : \"aid\", \"jvmId\" : \"jid1\", \"jvmPid\" : 1," +
                " \"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" : " +
                "\"-9223372036854775808\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\", " +
                "\"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\", " +
                "\"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\" :" +
                " [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, { \"key\"" +
                " : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\"," +
                " \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\", " +
                "\"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" :" +
                " \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" :" +
                " \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\"" +
                " : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" :" +
                " \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\", \"value\" :" +
                " \"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", \"value\" :" +
                " \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], \"uid\" : 1000," +
                " \"username\" : \"user\", \"systemId\" : \"1\" }] }";
        assertEquals(expected, getResponse.getContentAsString());
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
        String expected = "{ \"response\" : [{ \"agentId\" : \"aid\", \"jvmId\" : \"jid2\", \"jvmPid\" : 2, " +
                "\"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : { \"$numberLong\" : " +
                "\"1495727607482\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\"," +
                " \"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\"," +
                " \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\" " +
                ": [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, { " +
                "\"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\"," +
                " \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\"," +
                " \"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" :" +
                " \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\" " +
                ": \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" :" +
                " \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\", \"value\" : " +
                "\"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", \"value\" :" +
                " \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], \"uid\" : 1000," +
                " \"username\" : \"user\", \"systemId\" : \"1\" }] }";
        assertEquals(expected, getResponse.getContentAsString());
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
        String expected = "{ \"response\" : [{ \"lastUpdated\" : 2000 }] }";
        assertEquals(expected, response.getContentAsString());

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
        String expected = "{ \"response\" : [{\"systemId\":\"1\", \"jvms\":[{ \"agentId\" : \"aid\", \"jvmId\" : " +
                "\"jid1\", \"jvmPid\" : 1, \"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : {" +
                " \"$numberLong\" : \"-9223372036854775808\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\", " +
                "\"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\", " +
                "\"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\" :" +
                " [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, { \"key\"" +
                " : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\", " +
                "\"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\", " +
                "\"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" : " +
                "\"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\"" +
                " : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" : " +
                "\"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\", \"value\" : " +
                "\"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", \"value\" :" +
                " \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], \"uid\" : 1000," +
                " \"username\" : \"user\", \"systemId\" : \"1\" }]}]}";
        assertEquals(expected, response.getContentAsString());
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
        String expected = "{ \"response\" : [{\"systemId\":\"1\", \"jvms\":[{ \"jvmId\" : \"jid1\" }]}]}";
        assertEquals(expected, response.getContentAsString());
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
        String expected = "{ \"response\" : [{\"systemId\":\"1\", \"jvms\":[{ \"agentId\" : \"aid\", \"jvmId\" : " +
                "\"jid1\", \"jvmPid\" : 1, \"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : {" +
                " \"$numberLong\" : \"-9223372036854775808\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\", " +
                "\"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\", " +
                "\"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\" : " +
                "[{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, { \"key\" :" +
                " \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : \"GDMSESSION\", \"value\"" +
                " : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" : \"TERM\", \"value\" : " +
                "\"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, { \"key\" : \"LANG\", " +
                "\"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", \"value\" : " +
                "\"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, { \"key\" :" +
                " \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", \"value\" : \"i3\"" +
                " }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\", \"value\" : \"0\" }, " +
                "{ \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", \"value\" : " +
                "\"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], \"uid\" : 1000, " +
                "\"username\" : \"user\" }]}]}";
        assertEquals(expected, response.getContentAsString());
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
        String expected = "{ \"response\" : [{\"systemId\":\"1\", \"jvms\":[{ \"agentId\" : \"aid\", \"jvmId\" : " +
                "\"jid1\", \"jvmPid\" : 1, \"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" : {" +
                " \"$numberLong\" : \"-9223372036854775808\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" :" +
                " \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\", " +
                "\"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\"," +
                " \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\"" +
                " : [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }," +
                " { \"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : " +
                "\"GDMSESSION\", \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" " +
                ": \"TERM\", \"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, {" +
                " \"key\" : \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", " +
                "\"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }, " +
                "{ \"key\" : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", " +
                "\"value\" : \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\"," +
                " \"value\" : \"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", " +
                "\"value\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], " +
                "\"uid\" : 1000, \"username\" : \"user\", \"systemId\" : \"1\" },{ \"agentId\" : \"aid\", \"jvmId\" " +
                ": \"jid2\", \"jvmPid\" : 2, \"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" " +
                ": { \"$numberLong\" : \"1495727607482\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : " +
                "\"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\", " +
                "\"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\"," +
                " \"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\"" +
                " : [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }," +
                " { \"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : " +
                "\"GDMSESSION\", \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\" " +
                ": \"TERM\", \"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }," +
                " { \"key\" : \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", " +
                "\"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\"" +
                " }, { \"key\" : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\"," +
                " \"value\" : \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\"," +
                " \"value\" : \"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\"," +
                " \"value\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }]," +
                " \"uid\" : 1000, \"username\" : \"user\", \"systemId\" : \"1\" }]}]}";
        assertEquals(expected, response.getContentAsString());
    }

    @Test
    public void testTreeAllOffset() throws InterruptedException, TimeoutException, ExecutionException {
        String postUrl = jvmsUrl + "/systems/1";
        String treeUrl = jvmsUrl + "/tree";

        ContentResponse postResponse = client.newRequest(postUrl).method(HttpMethod.POST)
                .content(new StringContentProvider(postData), "application/json").send();
        assertEquals(200, postResponse.getStatus());

        String query = "?aliveOnly=false&offset=1";
        ContentResponse response = client.newRequest(treeUrl + query).method(HttpMethod.GET).send();
        assertEquals(200, response.getStatus());
        String expected = "{ \"response\" : [{\"systemId\":\"1\", \"jvms\":[{ \"agentId\" : \"aid\", \"jvmId\" :" +
                " \"jid2\", \"jvmPid\" : 2, \"startTime\" : { \"$numberLong\" : \"1495727607481\" }, \"stopTime\" :" +
                " { \"$numberLong\" : \"1495727607482\" }, \"javaVersion\" : \"1.8.0_131\", \"javaHome\" : " +
                "\"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre\", \"mainClass\" : \"mc\", " +
                "\"javaCommandLine\" : \"j cl\", \"jvmName\" : \"vm\", \"vmArguments\" : \"-Djline.log.jul=true\", " +
                "\"jvmInfo\" : \"mixed mode\", \"lastUpdated\" : 0, \"jvmVersion\" : \"25.131-b12\", \"environment\"" +
                " : [{ \"key\" : \"PATH\", \"value\" : \"/usr/local/bin:/usr/local/sbin:/usr/bin:/usr/sbin\" }, { " +
                "\"key\" : \"XAUTHORITY\", \"value\" : \"/run/user/1000/gdm/Xauthority\" }, { \"key\" : " +
                "\"GDMSESSION\", \"value\" : \"i3\" }, { \"key\" : \"fish_greeting\", \"value\" : \"\" }, { \"key\"" +
                " : \"TERM\", \"value\" : \"xterm-256color\" }, { \"key\" : \"DARWIN_MODE\", \"value\" : \"0\" }, {" +
                " \"key\" : \"LANG\", \"value\" : \"en_US.UTF-8\" }, { \"key\" : \"DBUS_SESSION_BUS_ADDRESS\", " +
                "\"value\" : \"unix:path=/run/user/1000/bus\" }, { \"key\" : \"XDG_SESSION_ID\", \"value\" : \"2\" }," +
                " { \"key\" : \"XDG_SESSION_TYPE\", \"value\" : \"x11\" }, { \"key\" : \"XDG_CURRENT_DESKTOP\", " +
                "\"value\" : \"i3\" }, { \"key\" : \"DISPLAY\", \"value\" : \":0\" }, { \"key\" : \"CYGWIN_MODE\", " +
                "\"value\" : \"0\" }, { \"key\" : \"COLORTERM\", \"value\" : \"truecolor\" }, { \"key\" : \"_\", " +
                "\"value\" : \"/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.131-1.b12.fc24.x86_64/jre/../bin/java\" }], " +
                "\"uid\" : 1000, \"username\" : \"user\", \"systemId\" : \"1\" }]}]}";
        assertEquals(expected, response.getContentAsString());
    }
}
