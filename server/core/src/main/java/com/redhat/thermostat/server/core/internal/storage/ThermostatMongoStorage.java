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

package com.redhat.thermostat.server.core.internal.storage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import com.redhat.thermostat.server.core.internal.configuration.ServerConfiguration;

public class ThermostatMongoStorage {
    private static MongoClient mongoClient;

    private static String username = "mongodevuser";
    private static char[] password = "mongodevpassword".toCharArray();
    private static String dbName = "thermostat";
    private static String host = "127.0.0.1";
    private static int port = 27518;

    public static void start(Map<String, String> serverConfiguration) {

        if (serverConfiguration.containsKey(ServerConfiguration.MONGO_DB)) {
            dbName = serverConfiguration.get(ServerConfiguration.MONGO_DB);
        }
        if (serverConfiguration.containsKey(ServerConfiguration.MONGO_USERNAME)) {
            username = serverConfiguration.get(ServerConfiguration.MONGO_USERNAME);
        }
        if (serverConfiguration.containsKey(ServerConfiguration.MONGO_PASSWORD)) {
            password = serverConfiguration.get(ServerConfiguration.MONGO_PASSWORD).toCharArray();
        }
        if (serverConfiguration.containsKey(ServerConfiguration.MONGO_URL)) {
            try {
                URL url = new URL(serverConfiguration.get(ServerConfiguration.MONGO_URL));
                host = url.getHost();
                port = url.getPort();
            } catch (MalformedURLException e) {
                //Do nothing. Defaults will be used
            }
        }

        MongoCredential credential = MongoCredential.createCredential(username, dbName, password);
        ServerAddress address = new ServerAddress(host, port);
        mongoClient = new MongoClient(address, Collections.singletonList(credential), new MongoClientOptions.Builder().serverSelectionTimeout(0).connectTimeout(0).socketTimeout(0).build());
    }

    public static boolean isConnected() {
        try {
            mongoClient.getAddress();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static void finish() {
        mongoClient.close();
    }

    public static MongoDatabase getDatabase() {
        return ThermostatMongoStorage.mongoClient.getDatabase(ThermostatMongoStorage.dbName);
    }
}