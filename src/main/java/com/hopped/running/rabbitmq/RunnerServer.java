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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.rabbitmq.client.QueueingConsumer;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 */
public class RunnerServer {

    public static final String RPC_QUEUE_NAME = "runningRabbit";
    final static Logger logger =
        LoggerFactory.getLogger(RunnerServer.class);

    public static void main(String[] args) throws Exception {
        final UserDatabase userDatabase = new UserDatabase();
        final RunnerSession session = new RunnerSession();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        channel.basicQos(1);

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

        logger.info(" [x] Handling RPC requests ...");

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

            BasicProperties props = delivery.getProperties();
            BasicProperties replyProps = new BasicProperties.Builder()
                .correlationId(props.getCorrelationId())
                .build();

            logger.info(" [1] parse incoming request ...");

            AuthRequest request = AuthRequest.parseFrom(delivery.getBody());

            logger.info(" [2] build response ...");

            AuthResponse.Builder response = AuthResponse.newBuilder();

            User user = userDatabase.getUser(
                request.getUsername(),
                request.getPassword());
            Error error = user.getError();
            if (error != null) {
                response.setError(error);
                //user.setCountLoginFailure((int) user.getCountLoginFailure() + 1);
            } else {
                response.setSessionId("s123");
                response.setUser(user);
                response.setSessionTimeout(30*60); // 30 minutes
                session.addSession(user, 30*60);
            }
            AuthResponse result = response.build();

            byte[] payload = result.toByteArray();

            logger.info(" [>] send outgoing response ...");

            channel.basicPublish("", props.getReplyTo(), replyProps, payload);
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }
    }
}
