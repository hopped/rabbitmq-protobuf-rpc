/*
 * Copyright (C) 2014 Dennis Hoppe. All Rights Reserved.
 */
package com.hopped.running.demo;

import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hopped.running.rabbitmq.RunnerClient;
import com.hopped.running.protobuf.RunnerProtos.User;
import com.hopped.running.protobuf.RunnerProtos.AuthRequest;
import com.hopped.running.protobuf.RunnerProtos.AuthResponse;
import com.hopped.running.protobuf.RunnerProtos.Error;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 * @version $Id:$
 */
public class Demo {

    final static Logger logger = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) throws Exception {
        logger.info("RabbitMQ RPC example (Java <> Perl)");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final String requestQueueName = "runningRabbit";

        /* AUTHENTICATION */

        RunnerClient client = new RunnerClient(factory, requestQueueName);
        try {
            client.init();
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
            return;
        }

        AuthRequest authRequest = AuthRequest.newBuilder()
            .setUsername("hoppe")
            .setPassword("is_fantastic")
            .build();
        logger.info("Logging in 'hoppe' ...");

        AuthResponse response = client.getResponse(authRequest);
        Error error = response.getError();
        if (error != null) {
            logger.error(error.getErrorMessage());
        } else {
            User user = response.getUser();
            logger.info("  User 'hoppe' logged in.");
            logger.info("  Hello " + user.getFirstName());
        }

        /* TODO: GET LIST OF RUNS */



        logger.info("Done.");
    }
}
