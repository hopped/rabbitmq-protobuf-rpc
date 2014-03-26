/*
 * Copyright (C) 2014 Dennis Hoppe. All Rights Reserved.
 */
package com.hopped.running.rabbitmq;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hopped.running.protobuf.RunnerProtos.AuthRequest;
import com.hopped.running.protobuf.RunnerProtos.AuthResponse;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 * @version $Id:$
 */
public class RunnerClient {

    final static Logger logger =
        LoggerFactory.getLogger(RunnerClient.class);

    private Channel channel;
    private Connection connection;
    private final ConnectionFactory factory;
    private String replyQueueName;
    private final String requestQueueName;
    private QueueingConsumer consumer;

    /**
     *
     */
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

    /**
     * @throws IOException
     */
    public void init() throws IOException {
        connection = factory.newConnection();
        channel = connection.createChannel();
        replyQueueName = channel.queueDeclare().getQueue();
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
    }

    /**
     * @throws Exception
     */
    public void close() throws Exception {
        connection.close();
    }

    /**
     * @throws Exception
     */
    public AuthResponse getResponse(AuthRequest query) throws Exception {
        AuthResponse response = null;

        String corrId = java.util.UUID.randomUUID().toString();

        BasicProperties props = new BasicProperties.Builder()
            .correlationId(corrId)
            .replyTo(replyQueueName)
            .build();

        logger.info(" [>] send authentication request to queue: "
            + requestQueueName);

        channel.basicPublish("", requestQueueName, props, query.toByteArray());

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                logger.info(" [<] received authentication result");

                response = AuthResponse.parseFrom(delivery.getBody());
                break;
            }
        }

        return response;
    }

    public final ConnectionFactory getConnectionFactory() {
        return factory;
    }

    public final String getRequestQueueName() {
        return requestQueueName;
    }

}
