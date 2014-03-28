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

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author Dennis Hoppe (hoppe.dennis@ymail.com)
 * 
 */
public abstract class ARunnerRPCServer {

    final static Logger logger = LoggerFactory
            .getLogger(ARunnerRPCServer.class);

    private final String queueName;
    private final Channel channel;
    protected Class<?> protocol;
    protected Object instance;

    private QueueingConsumer consumer;

    /**
     * 
     * @param host
     * @param queueName
     * @throws IOException
     */
    public ARunnerRPCServer(final Channel channel, final String queueName)
            throws IOException {
        this.channel = channel;
        this.queueName = (queueName == null || queueName.isEmpty()) ?
                channel.queueDeclare().getQueue() : queueName;
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    public ARunnerRPCServer init() throws IOException {
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumer);

        return this;
    }

    public ARunnerRPCServer setProtocol(Class<?> protocol) {
        this.protocol = protocol;
        return this;
    }

    public ARunnerRPCServer setInstance(Object instance) {
        this.instance = instance;
        return this;
    }

    /**
     * 
     */
    public void closeConnection() {
        if (consumer != null) {
            try {
                channel.basicCancel(consumer.getConsumerTag());
                consumer = null;
            } catch (Exception e) {
                // ignore
            }
        }
    }

    /**
     * 
     */
    public void consume() {
        while (true) {
            try {
                logger.info("ARunnerRPCServer::consume");
                Delivery delivery = consumer.nextDelivery();
                BasicProperties props = delivery.getProperties();
                BasicProperties replyProps = new BasicProperties.Builder()
                        .correlationId(props.getCorrelationId())
                        .build();

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                byte[] payload = processRequest(delivery);

                channel.basicPublish("", props.getReplyTo(), replyProps,
                        payload);

            } catch (ShutdownSignalException | ConsumerCancelledException
                    | InterruptedException | IOException e) {
                // handle error
            }
        }
    }

    /**
     * 
     * @param delivery
     * @return
     */
    public abstract byte[] processRequest(Delivery delivery);

}
