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
package com.hopped.running.rabbitmq.rpc;

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
public abstract class ARPCServer<T extends ARPCServer<T>> {

    private final static Logger logger = LoggerFactory
            .getLogger(ARPCServer.class);

    private final Channel channel;
    private String queueName;
    private QueueingConsumer consumer;

    protected Object instance;
    protected Class<?> protocol;

    /**
     * 
     * @param host
     * @param queueName
     * @throws IOException
     */
    public ARPCServer(final Channel channel, final String queueName) {
        this.channel = channel;
        this.queueName = queueName;
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    public T init() throws IOException {
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumer);
        queueName = (queueName == null || queueName.isEmpty()) ?
                channel.queueDeclare().getQueue() : queueName;
        return self();
    }

    /**
     * 
     */
    private void checkConsumer() {
        if (consumer == null) {
            throw new IllegalStateException(
                    "Consumer is not initialized; call init() first, please.");
        }
    }

    /**
     * 
     */
    public void consume() {
        checkConsumer();

        while (true) {
            try {
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
                logger.error(e.getMessage());
                closeConnection();
            }
        }
    }

    /**
     * 
     * @param delivery
     * @return
     */
    public abstract byte[] processRequest(Delivery delivery);

    /**
     * 
     * @return
     */
    public abstract T self();

    /**
     * 
     */
    public void closeConnection() {
        checkConsumer();
        try {
            channel.basicCancel(consumer.getConsumerTag());
            consumer = null;
        } catch (IOException e) {
            logger.warn(e.getMessage());
            // ignore exception
        }
    }

    /**
     * 
     * @param protocol
     * @return
     */
    public T setProtocol(Class<?> protocol) {
        this.protocol = protocol;
        return self();
    }

    /**
     * 
     * @param instance
     * @return
     */
    public T setInstance(Object instance) {
        this.instance = instance;
        return self();
    }

    /**
     * 
     * @return
     */
    public final String getQueueName() {
        return queueName;
    }

    /**
     * 
     * @return
     */
    public final Class<?> getProtocol() {
        return protocol;
    }

    /**
     * 
     * @return
     */
    public final Object getInstance() {
        return instance;
    }

}
