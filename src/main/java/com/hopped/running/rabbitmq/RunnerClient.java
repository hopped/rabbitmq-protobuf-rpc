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
package com.hopped.running.rabbitmq;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hopped.running.protobuf.RunnerProtos.AuthRequest;
import com.hopped.running.protobuf.RunnerProtos.AuthResponse;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/**
 * @author Dennis Hoppe (hoppe.dennis@ymail.com)
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
