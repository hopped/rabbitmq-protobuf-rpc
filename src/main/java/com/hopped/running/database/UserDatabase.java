/*!
 * Copyright (c) 2014 Dennis Hoppe
 * www.dennis-hoppe.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.hopped.running.database;

import java.util.HashMap;
import java.util.Map;

import com.hopped.running.protobuf.RunnerProtos.User;

/**
 * @author Dennis Hoppe (hoppe.dennis@ymail.com)
 */
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
