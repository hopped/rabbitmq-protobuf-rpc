/*
 * Copyright (C) 2014 Dennis Hoppe. All Rights Reserved.
 */
package com.hopped.rabbitmq;

import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 * @version $Id:$
 */
public class Main {

    final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("RabbitMQ RPC example (Java <> Perl)");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final String requestQueueName = "runningRabbit";
        RunnerClient client = new RunnerClient(factory, requestQueueName);
        try {
            client.init();
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
            return;
        }
    }
}
