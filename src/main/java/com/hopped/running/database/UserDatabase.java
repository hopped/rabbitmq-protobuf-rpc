/*
 * Copyright (C) 2014 Dennis Hoppe. All Rights Reserved.
 */
package com.hopped.running.database;

import java.util.Map;
import java.util.HashMap;
import com.hopped.running.protobuf.RunnerProtos.User;

public class UserDatabase {

    private Map<String, User> database;

    public UserDatabase() {
        database = new HashMap<String, User>();

        /* build database */
        User hopped = User.newBuilder()
            .setAlias("hopped")
            .setId(1)
            .setFirstName("Dennis")
            .setLastName("Hoppe")
            .setPassword("is_fantastic")
            .build();
        database.put("hopped<>is_fantastic", hopped);
    }

    public User getUser(final String username, final String password) {
        return database.get(username + "<>" + password);
    }

    public void addUser(final User user) {
        if (getUser(user.getAlias(), user.getPassword()) != null) {
            throw new IllegalStateException(
                "user '" + user.getAlias()+ "' already exists");
        }
        database.put(user.getAlias() + "<>" + user.getPassword(), user);
    }

}
