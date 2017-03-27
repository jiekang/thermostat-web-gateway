package com.redhat.thermostat.gateway.common.mongodb.filters;

import org.bson.conversions.Bson;

import com.mongodb.BasicDBObject;

public class MongoSortFilters {

    public static Bson createSortObject(String sort) {
        BasicDBObject sortObject = new BasicDBObject();
        if (sort != null) {
            String[] items = sort.split(",");
            for (String item : items) {
                if (item.charAt(0) == '+') {
                    sortObject.append(item.substring(1), 1);
                } else if (item.charAt(0) == '-') {
                    sortObject.append(item.substring(1), -1);
                }
            }
        }
        return sortObject;
    }
}
