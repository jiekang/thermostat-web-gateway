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


package com.redhat.thermostat.gateway.service.jvm.gc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.redhat.thermostat.gateway.common.mongodb.servlet.RequestParameters;
import com.redhat.thermostat.gateway.tests.integration.MongoIntegrationTest;
import org.bson.Document;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

public class JvmGcServiceIntegrationTest extends MongoIntegrationTest {

    private static final String serviceName = "jvm-gc";
    private static final String versionNumber = "0.0.2";
    private static final String gcUrl = baseUrl + "/" + serviceName + "/" + versionNumber;
    private final String data = "[{ \"a\" : \"test\", \"b\" : \"test1\", \"c\" : \"test2\" }, { \"d\" : \"test3\"}," +
            "{\"e\" : \"test4\" }]";

    private static final String QUERY_PREFIX = "query";
    private static final String LIMIT_PREFIX = "limit";
    private static final String SORT_PREFIX = "sort";
    private static final String OFFSET_PREFIX = "offset";
    private static final String METADATA_PREFIX = "metadata";
    private static final String INCLUDE_PREFIX = "include";
    private static final String IEXCLUDE_PREFIX = "exclude";

    public JvmGcServiceIntegrationTest() {
        super(serviceName + "/" + versionNumber, serviceName);
    }

    private void makeHttpGetRequest(String url, String expectedResponse, int expectedStatus)
            throws InterruptedException, TimeoutException, ExecutionException {

        ContentResponse response = client.newRequest(url).method(HttpMethod.GET).send();

        if (!expectedResponse.equals("")) {
            assertEquals(expectedStatus, response.getStatus());
            assertEquals(expectedResponse, response.getContentAsString());
        }
    }

    private void makeHttpMethodRequest(HttpMethod httpMethod, String urlQuery, String dataToSend, String dataType,
                                       String expectedResponse, int expectedStatus)
            throws InterruptedException, TimeoutException, ExecutionException {

        StringContentProvider stringContentProvider = new StringContentProvider(dataToSend, "UTF-8");
        ContentResponse postResponse = client.newRequest(gcUrl + urlQuery).method(httpMethod)
                .content(stringContentProvider, dataType)
                .send();
        assertEquals(expectedStatus, postResponse.getStatus());

        if (!expectedResponse.equals("")) {
            makeHttpGetRequest(gcUrl, expectedResponse, expectedStatus);
        }
    }

    @Test
    public void testGet() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl,"{\"response\":[{\"a\":\"test\",\"b\":\"test1\","+
                "\"c\":\"test2\"}]}", 200);
    }

    @Test
    public void testGetLimitParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + LIMIT_PREFIX + "=2","{\"response\":[{\"a\":\"test\",\"b\":"+
                "\"test1\",\"c\":\"test2\"},{\"d\":\"test3\"}]}", 200);
    }

    @Test
    public void testGetSortParam() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"1\"}, {\"a\":\"2\"}]";

        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + LIMIT_PREFIX + "=2&" + SORT_PREFIX + "=-a","{\"response\":[{\"a\":\"2\"},{\"a\":"+
                "\"1\"}]}", 200);
    }

    @Test
    public void testGetProjectParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + INCLUDE_PREFIX + "=b,c","{\"response\":[{\"b\":\"test1\",\"c\":"+
                "\"test2\"}]}", 200);
    }

    @Test
    public void testGetOffsetParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + OFFSET_PREFIX + "=1","{\"response\":[{\"d\":\"test3\"}]}", 200);
    }

    @Test
    public void testGetQueryParam() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + QUERY_PREFIX + "=b==test1","{\"response\":[{\"a\":\"test\",\"b\":"+
                "\"test1\",\"c\":\"test2\"}]}", 200);
    }

    @Test
    public void testPostJSON() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST, '?' + METADATA_PREFIX + "=true","[{\"f1\":\"test\"}]","application/json",
                "{\"response\":[{\"f1\":\"test\"}]}", 200);
    }

    @Test
    public void testPostXML() throws InterruptedException,TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST,"","","application/xml","", 415);
    }

    @Test
    public void testInvalidDataPost() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST,"","{\"badFormat\":\"missing square brackets\"}",
                "application/json","", 400);
    }

    @Test
    public void testDelete() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.DELETE,"","","","", 200);
    }

    @Test
    public void testNonExistentDataDelete() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.DELETE,'?' + QUERY_PREFIX + "=nonExist==Null","","","", 200);
    }

    @Test
    public void testPostPutDeleteMockedData() throws InterruptedException, TimeoutException, ExecutionException {
        makeHttpMethodRequest(HttpMethod.POST,"","[{\"f1\":\"test\"}]","application/json",
                "{\"response\":[{\"f1\":\"test\"}]}", 200);

        makeHttpMethodRequest(HttpMethod.PUT,'?' + QUERY_PREFIX + "=f1==test","{\"set\": {\"f1\":\"newdata\"}}",
                "application/json","{\"response\":[{\"f1\":\"newdata\"}]}", 200);

        makeHttpMethodRequest(HttpMethod.DELETE,"?q=f1==test","","","", 200);
    }

    @Test
    public void testGetWithMetaDataAndLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}"],
        //"metaData":{"payloadCount":1,"count":3,
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=1&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":3,"+
                "\"next\":\"" + gcUrl + "?" + OFFSET_PREFIX + "\\u003d1\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX  +"\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1", expectedResponse, 200);
    }

    @Test
    public void testGetWithMetaDataAndOffset() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}"],"metaData":{"payloadCount":1,"count":3,
        //"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=1",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=2&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"b\":\"test1\"}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":3,"+
                "\"prev\":\"" + gcUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d1\","+
                "\"next\":\"" + gcUrl + "?" + OFFSET_PREFIX + "\\u003d2\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + OFFSET_PREFIX + "=1", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatPrevOffsetBiggerThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":1,"count":6,"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=2&o=1",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=5&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"b\":\"test1\"},{\"b\":\"test1\"}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":6,"+
                "\"prev\":\"" + gcUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d1\","+
                "\"next\":\"" + gcUrl + "?" + OFFSET_PREFIX + "\\u003d5\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=2&" + OFFSET_PREFIX + "=3", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatPrevOffsetEqualToLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":1,"count":3,"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=1",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=2&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"b\":\"test1\"}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":3,"+
                "\"prev\":\"" + gcUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d1\","+
                "\"next\":\"" + gcUrl + "?" + OFFSET_PREFIX + "\\u003d2\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=1&" + OFFSET_PREFIX + "=1", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatNextOffsetLessThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"e\":\"test4\",\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":2,"count":6,"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=1&o=0",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=3&l=2&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"b\":\"test1\"},{\"e\":\"test4\",\"b\":\"test1\"}],"+
                "\"metaData\":{\"payloadCount\":2,\"count\":6,"+
                "\"prev\":\"" + gcUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + OFFSET_PREFIX + "\\u003d0\","+
                "\"next\":\"" + gcUrl + "?" + OFFSET_PREFIX + "\\u003d3\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=2&" + OFFSET_PREFIX + "=1", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatNextOffsetBiggerThanLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";

        // {"response":["{\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":1,"count":6,
        //"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=2&o=1",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=5&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"b\":\"test1\"},{\"b\":\"test1\"}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":6,"+
                "\"prev\":\"" + gcUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d1\","+
                "\"next\":\"" + gcUrl + "?" + OFFSET_PREFIX + "\\u003d5\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=2&" + OFFSET_PREFIX + "=3", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatNextOffsetEqualToLimit() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";

        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":2,"count":6,"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=2&o=0",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=4&l=2&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"},{\"b\":\"test1\"}],"+
                "\"metaData\":{\"payloadCount\":2,\"count\":6,"+
                "\"prev\":\"" + gcUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d0\","+
                "\"next\":\"" + gcUrl + "?" + OFFSET_PREFIX + "\\u003d4\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";

        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=2&" + OFFSET_PREFIX + "=2", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatNextExtremity() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";

        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":2,"count":6,
        //"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=2&o=0",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=4&l=2&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"},{\"b\":\"test1\"}],"+
                "\"metaData\":{\"payloadCount\":2,\"count\":6,"+
                "\"prev\":\"" + gcUrl + "?" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d0\","+
            "\"next\":\"" + gcUrl + "?" + OFFSET_PREFIX + "\\u003d4\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";
        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=2&" + OFFSET_PREFIX + "=2", expectedResponse, 200);
    }

    @Test
    public void testGetMetDatPrevExtremity() throws InterruptedException, TimeoutException, ExecutionException {
        String data ="[{\"a\":\"test\",\"b\":\"test1\",\"c\":\"test2\"}, {\"b\":\"test1\"},"+
                "{\"e\":\"test4\",\"b\":\"test1\"}, {\"b\":\"test1\"}, {\"b\":\"test1\"},"+
                "{\"b\":\"test1\"}]";


        // {"response":["{\"e\":\"test4\",\"b\":\"test1\"}","{\"b\":\"test1\"}","{\"b\":\"test1\"}"],
        //"metaData":{"payloadCount":1,"count":6,"prev":"http://127.0.0.1:30000/jvm-gc/0.0.2?q===test1&m=true&l=2&o=0",
        //"next":"http://127.0.0.1:30000/jvm-gc/0.0.2?o=5&l=1&q===test1&m=true"}}
        String expectedResponse ="{\"response\":[{\"e\":\"test4\",\"b\":\"test1\"},"+
                "{\"b\":\"test1\"},{\"b\":\"test1\"}],"+
                "\"metaData\":{\"payloadCount\":1,\"count\":6,"+
                "\"prev\":\"" + gcUrl + "?" +QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\\u0026" + LIMIT_PREFIX + "\\u003d2\\u0026" + OFFSET_PREFIX + "\\u003d0\","+
                "\"next\":\"" + gcUrl + "?" + OFFSET_PREFIX + "\\u003d5\\u0026" + LIMIT_PREFIX + "\\u003d1\\u0026" + QUERY_PREFIX + "\\u003db\\u003d\\u003dtest1\\u0026" + METADATA_PREFIX + "\\u003dtrue\"}}";
        makeHttpMethodRequest(HttpMethod.POST,"", data,"application/json","", 200);
        makeHttpGetRequest(gcUrl + '?' + QUERY_PREFIX + "=b==test1&" + METADATA_PREFIX + "=true&" + LIMIT_PREFIX + "=3&" + OFFSET_PREFIX + "=2", expectedResponse, 200);
    }

    @Test
    public void testUpdateDoesNotAffectRealms() throws InterruptedException, TimeoutException, ExecutionException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\",\"b\"]}," +
                "{\"item\":2,\"realms\":[\"a\",\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        String updateString = "{\"set\" : {\"realms\" : 1, \"a\" : 2}}";
        makeHttpMethodRequest(HttpMethod.PUT,"", updateString,"application/json","", 200);

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                assertEquals("[\"a\",\"b\"]", gson.toJson(document.get("realms"), listType));
            }
        });
    }

    @Test
    public void testUpdateOnlyRealmsDoesNotAffectRealms() throws InterruptedException, TimeoutException, ExecutionException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\",\"b\"]}," +
                "{\"item\":2,\"realms\":[\"a\",\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        String updateString = "{\"set\" : {\"realms\" : 1}}";
        makeHttpMethodRequest(HttpMethod.PUT,"", updateString,"application/json","", 400);

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                assertEquals("[\"a\",\"b\"]", gson.toJson(document.get("realms"), listType));
            }
        });
    }

    @Test
    public void testUpdateRealmsQueryMatchIsNotUsed() throws InterruptedException, TimeoutException, ExecutionException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\"]}," +
                "{\"item\":2,\"realms\":[\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        String updateString = "{\"set\" : {\"item\" : 5}}";
        StringContentProvider stringContentProvider = new StringContentProvider(updateString, "UTF-8");
        ContentResponse response = client.newRequest(gcUrl).param(RequestParameters.QUERY, "realms==[\"a\"]")
                .method(HttpMethod.PUT).content(stringContentProvider, "application/json")
                .send();
        assertEquals(400, response.getStatus());

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                if (document.get("item", Double.class).equals(5)) {
                    fail();
                }
            }
        });
    }

    @Test
    public void testUpdateMultipleRealmsDoesNotAffectRealms() throws InterruptedException, TimeoutException, ExecutionException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\",\"b\"]}," +
                "{\"item\":2,\"realms\":[\"a\",\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        String updateString = "{\"set\" : {\"realms\" : 1, \"realms\" : 2}}";
        makeHttpMethodRequest(HttpMethod.PUT,"", updateString,"application/json","", 400);

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                assertEquals("[\"a\",\"b\"]", gson.toJson(document.get("realms"), listType));
            }
        });
    }

    @Test
    public void testGetCannotSeeRealms() throws InterruptedException, ExecutionException, TimeoutException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\",\"b\"]}," +
                "{\"item\":2,\"realms\":[\"a\",\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        String expected = "{\"response\":[{\"item\":1.0}]}";

        makeHttpGetRequest(gcUrl,expected, 200);
    }

    @Test
    public void testGetProjectionCannotSeeRealms() throws InterruptedException, ExecutionException, TimeoutException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\",\"b\"]}," +
                "{\"item\":2,\"realms\":[\"a\",\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        String expected = "{\"response\":[{\"item\":1.0}]}";

        makeHttpGetRequest(gcUrl + "?" + RequestParameters.INCLUDE + "=realms,item",expected, 200);
    }

    @Test
    public void testGetQueryCannotMatchRealms() throws InterruptedException, ExecutionException, TimeoutException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\"]}," +
                "{\"item\":2,\"realms\":[\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        ContentResponse response = client.newRequest(gcUrl)
                .param(RequestParameters.QUERY, "realms==[\"a\"]").method(HttpMethod.GET).send();

        assertEquals(400, response.getStatus());
    }


    @Test
    public void testPostCannotAddRealms() throws InterruptedException, ExecutionException, TimeoutException {
        String data = "[{\"item\":1,\"realms\":[\"a\",\"b\"]}," +
                "{\"item\":2,\"realms\":[\"a\",\"b\"]}]";

        makeHttpMethodRequest(HttpMethod.POST, "", data, "application/json", "", 200);

        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                assertNull(document.get("realms"));
            }
        });
    }

    @Test
    public void testDeleteCannotQueryRealms() throws InterruptedException, TimeoutException, ExecutionException {
        MongoCollection<Document> collection = mongodTestUtil.getCollection(serviceName);
        String data = "[{\"item\":1,\"realms\":[\"a\",\"b\"]}," +
                "{\"item\":2,\"realms\":[\"a\",\"b\"]}]";
        final Gson gson = new GsonBuilder().create();
        final Type listType = new TypeToken<List<Document>>() {}.getType();
        List<Document> insertDocuments = gson.fromJson(data, listType);

        collection.insertMany(insertDocuments);

        ContentResponse response = client.newRequest(gcUrl)
                .param(RequestParameters.QUERY, "realms==[\"a\",\"b\"]").method(HttpMethod.DELETE)
                .send();
        assertEquals(400, response.getStatus());

        FindIterable<Document> documents = collection.find();
        documents.forEach(new Block<Document>() {
            @Override
            public void apply(Document document) {
                assertEquals("[\"a\",\"b\"]", gson.toJson(document.get("realms"), listType));
            }
        });
    }
}
