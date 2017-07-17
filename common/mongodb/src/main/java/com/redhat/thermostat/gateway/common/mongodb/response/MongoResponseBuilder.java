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

package com.redhat.thermostat.gateway.common.mongodb.response;

import java.util.ArrayList;

import org.bson.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.Block;
import com.mongodb.client.FindIterable;
import com.redhat.thermostat.gateway.common.mongodb.keycloak.KeycloakFields;

/*
 *  Builds the appropriate response after executing the request's MongoDB Query.
 *
 *  NOTE: Builder fields that aren't explicitly set - and therefore null - are omitted in the
 *        serialized JSON.
 */
public class MongoResponseBuilder {

    private final ArrayList<Document> response;
    private final MongoMetaDataResponseBuilder metaData;

    public static class Builder {

        private ArrayList<Document> queryDocuments;
        private MongoMetaDataResponseBuilder metaData;
        private final Gson gson = new GsonBuilder().create();

        public Builder queryDocuments(FindIterable<Document> documents) {
            queryDocuments = new ArrayList<>();
            documents.forEach(new Block<Document>() {
                @Override
                public void apply(Document document) {
                    if (document.containsKey(KeycloakFields.REALMS_KEY)) {
                        document.remove(KeycloakFields.REALMS_KEY);
                    }
                    queryDocuments.add(document);
                }
            });
            return this;
        }

        public Builder metaData(MongoMetaDataResponseBuilder metaData) {
            this.metaData = metaData;
            return this;
        }

        public String build() {
            MongoResponseBuilder data = new MongoResponseBuilder(this);
            return gson.toJson(data);
        }
    }

    private MongoResponseBuilder(Builder builder) {
        response = builder.queryDocuments;
        metaData = builder.metaData;
    }
}
