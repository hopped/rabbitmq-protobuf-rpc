/*
 * Copyright (C) 2014 Dennis Hoppe. All Rights Reserved.
 */
package com.hopped.rabbitmq;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 * @version $Id:$
 */
public class RunnerClient {

    private Channel channel;
    private Connection connection;
    private final ConnectionFactory factory;
    private String replyQueueName;
    private final String requestQueueName;
    private QueueingConsumer consumer;

    public RunnerClient(final ConnectionFactory factory,
                        final String requestQueueName) {
        if (factory == null) {
            throw new IllegalArgumentException("factory is null");
        }
        if (requestQueueName == null) {
            throw new IllegalArgumentException("requestQueueName is null");
        } else if (requestQueueName.isEmpty()) {
            throw new IllegalStateException("requestQueueName is empty");
        }
        this.requestQueueName = requestQueueName;
        this.factory = factory;
    }

    public RunnerClient init() throws IOException {
        connection = factory.newConnection();
        channel = connection.createChannel();
        replyQueueName = channel.queueDeclare().getQueue();
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);

        return this;
    }

}
