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

package com.redhat.thermostat.gateway.common.mongodb.executor;

import org.bson.Document;

import com.mongodb.client.FindIterable;

public class MongoDataResultContainer {

    private long getReqCount;
    private long postReqMatches;
    private long postReqInsertions;
    private long putReqMatches;
    private long deleteReqMatches;
    private int remainingNumQueryDocuments;
    private FindIterable<Document> getReqQueryResult;

    public void setGetReqCount(long getReqCount) {
        this.getReqCount = getReqCount;
    }

    public void setQueryDataResult(FindIterable<Document> documents) {
        getReqQueryResult = documents;
    }

    public FindIterable<Document> getQueryDataResult() {
        return getReqQueryResult;
    }

    public void setPostReqInsertions(long postReqInsertions) {
        this.postReqInsertions = postReqInsertions;
    }

    public void setPostReqMatches(long postReqUpdates) {
        this.postReqMatches = postReqUpdates;
    }

    public void setPutReqMatches(long putReqUpdates) {
        this.putReqMatches = putReqUpdates;
    }

    public void setDeleteReqMatches(long deleteReqMatches) {
        this.deleteReqMatches = deleteReqMatches;
    }

    public void setRemainingNumQueryDocuments(int remainingNumQueryDocuments) {
        this.remainingNumQueryDocuments = remainingNumQueryDocuments;
    }

    public long getGetReqCount() {
        return this.getReqCount;
    }

    public long getPostReqInsertions() {
        return postReqInsertions;
    }

    public long getPostReqMatches() {
        return postReqMatches;
    }

    public long getPutReqMatches() {
        return putReqMatches;
    }

    public long getDeleteReqMatches() {
        return deleteReqMatches;
    }

    public int getRemainingNumQueryDocuments() {
        return remainingNumQueryDocuments;
    }

}