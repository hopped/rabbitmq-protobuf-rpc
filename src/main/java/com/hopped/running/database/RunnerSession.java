/*
 * Copyright (C) 2014 Dennis Hoppe. All Rights Reserved.
 */
package com.hopped.running.database;

import java.util.Map;
import java.util.HashMap;

import com.hopped.running.protobuf.RunnerProtos.User;

public class RunnerSession {

    private Map<String, Integer> database;

    public RunnerSession() {
        database = new HashMap<String, Integer>();
    }

    public boolean isSessionActive(final User user) {
        return (database.get(user.getAlias()) != null) ? true : false;
    }

    public void addSession(final User user, final int timeout) {
        database.put(user.getAlias(), timeout);
    }

}
