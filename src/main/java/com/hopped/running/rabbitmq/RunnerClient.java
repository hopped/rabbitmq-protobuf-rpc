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
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.hopped.running.demo.IRunnerService;
import com.hopped.running.protobuf.RunnerProtos.Ack;
import com.hopped.running.protobuf.RunnerProtos.AuthRequest;
import com.hopped.running.protobuf.RunnerProtos.AuthResponse;
import com.hopped.running.protobuf.RunnerProtos.User;
import com.hopped.running.rabbitmq.rpc.RPCMessage;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * @author Dennis Hoppe (hoppe.dennis@ymail.com)
 */
public class RunnerClient implements InvocationHandler {

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
    public RunnerClient init() throws IOException {
        connection = factory.newConnection();
        channel = connection.createChannel();
        replyQueueName = channel.queueDeclare().getQueue();
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);

        return this;
    }

    /**
     * @throws Exception
     */
    public void close() throws Exception {
        connection.close();
    }

    public Object createProxy(Class<?> protocol) {
        return Proxy.newProxyInstance(protocol.getClassLoader(),
                new Class[] { protocol }, this);
    }

    /**
     * @throws IOException
     * @throws InterruptedException
     * @throws ConsumerCancelledException
     * @throws ShutdownSignalException
     * @throws Exception
     */
    public byte[] basicCall(final byte[] query)
            throws IOException,
            ShutdownSignalException, ConsumerCancelledException,
            InterruptedException {

        String corrId = java.util.UUID.randomUUID().toString();

        BasicProperties props = new BasicProperties.Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        logger.info(" [>] send authentication request to queue: "
                + requestQueueName);

        channel.basicPublish("", requestQueueName, props, query);

        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                logger.info(" [<] received authentication result");

                return delivery.getBody();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
     * java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        logger.info("RunnerClient invoke: " + method.getName());

        Message msg = (Message) args[0];
        RPCMessage invoker = new RPCMessage()
                .setMethod(method.getName())
                .setRequestObject(msg);

        byte[] response = basicCall(invoker.toByteArray());

        String klassName = method.getReturnType().getName();
        Class<?> klass = this.getClass().getClassLoader().loadClass(klassName);
        Method parseFrom = klass.getMethod("parseFrom", byte[].class);

        return parseFrom.invoke(null, response);
    }

    public final ConnectionFactory getConnectionFactory() {
        return factory;
    }

    public final String getRequestQueueName() {
        return requestQueueName;
    }

    public static void main(final String... strings) throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        AuthRequest request = AuthRequest.newBuilder()
                .setUsername("hopped")
                .build();

        RunnerClient client = new RunnerClient(factory, "runningRabbit").init();
        IRunnerService service =
                (IRunnerService) client.createProxy(IRunnerService.class);
        AuthResponse res = service.login(request);
        System.out.println("Returned sessionId: " + res.getSessionId());

        Ack ack = service.setProfile(User.newBuilder().setAlias("hopped")
                .build());
        System.out.println(ack.getSuccess());
    }
}
