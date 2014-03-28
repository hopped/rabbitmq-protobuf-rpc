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

import com.google.protobuf.InvalidProtocolBufferException;
import com.hopped.running.database.RunnerSession;
import com.hopped.running.database.UserDatabase;
import com.hopped.running.protobuf.RunnerProtos.AuthRequest;
import com.hopped.running.protobuf.RunnerProtos.AuthResponse;
import com.hopped.running.protobuf.RunnerProtos.Error;
import com.hopped.running.protobuf.RunnerProtos.User;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 */
public class RunnerServer {

    final static Logger logger =
            LoggerFactory.getLogger(RunnerServer.class);

    private final UserDatabase userDatabase = new UserDatabase();
    private final RunnerSession session = new RunnerSession();
    private final String queueName;
    private final String host;

    private Connection connection;
    private Channel channel;
    private QueueingConsumer consumer;

    /**
     * 
     * @param host
     * @param queueName
     */
    public RunnerServer(final String host, final String queueName) {
        this.queueName = queueName;
        this.host = host;
    }

    /**
     * 
     * @return
     * @throws IOException
     */
    public RunnerServer init() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);

        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.basicQos(1);
        channel.queueDeclare(queueName, false, false, false, null);

        consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, false, consumer);

        logger.info(" [x] Handling RPC requests ...");

        return this;
    }

    /**
     * 
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
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
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                BasicProperties props = delivery.getProperties();
                BasicProperties replyProps = new BasicProperties.Builder()
                        .correlationId(props.getCorrelationId())
                        .build();

                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                byte[] payload = getResponse(delivery);
                channel.basicPublish("", props.getReplyTo(), replyProps,
                        payload);

                logger.info("Done.");
            } catch (ShutdownSignalException | ConsumerCancelledException
                    | InterruptedException | IOException e) {
                logger.error(e.getMessage());
            }

        }
    }

    /**
     * 
     * @param delivery
     * @return
     * @throws InvalidProtocolBufferException
     */
    public byte[] getResponse(QueueingConsumer.Delivery delivery)
            throws InvalidProtocolBufferException {
        logger.info(" [1] parse incoming request ...");
        AuthRequest request = AuthRequest.parseFrom(delivery.getBody());

        logger.info(" [2] build response ...");

        AuthResponse.Builder response = AuthResponse.newBuilder();

        User user = userDatabase.getUser(
                request.getUsername(),
                request.getPassword());
        Error error = user.getError();
        if (error.getErrorCode() > 0) {
            response.setError(error);
        } else {
            response.setSessionId("s123");
            response.setUser(user);
            response.setSessionTimeout(30 * 60); // 30 minutes
            session.addSession(user, 30 * 60);
        }
        AuthResponse result = response.build();

        return result.toByteArray();
    }

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        RunnerServer server = new RunnerServer("localhost", "runningRabbit");
        try {
            server.init().consume();
        } catch (IOException e) {
            logger.error(e.getMessage());
        } finally {
            if (server != null) {
                server.closeConnection();
            }
        }
    }
}
