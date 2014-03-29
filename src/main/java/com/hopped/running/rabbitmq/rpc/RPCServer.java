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
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Message;
import com.hopped.running.demo.IRunnerService;
import com.hopped.running.demo.RunnerServiceImpl;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer.Delivery;

/**
 * @author Dennis Hoppe (hoppe.dennis@ymail.com)
 * 
 */
public class RPCServer extends ARPCServer<RPCServer> {

    private final static Logger logger = LoggerFactory
            .getLogger(RPCServer.class);

    /**
     * 
     * @param channel
     * @param queueName
     */
    public RPCServer(Channel channel, String queueName) {
        super(channel, queueName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hopped.running.rabbitmq.rpc.ARPCServer#self()
     */
    @Override
    public RPCServer self() {
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hopped.running.rabbitmq.ARunnerRPCServer#processRequest(com.rabbitmq
     * .client.QueueingConsumer.Delivery)
     */
    @Override
    public byte[] processRequest(Delivery delivery) {
        logger.info("RunnerRPCServer::processRequest");

        try {
            Invoker invoker = Invoker.parseFrom(delivery.getBody());
            String name = invoker.getMethod();
            Message request = invoker.getRequestMessage();
            Method method = protocol.getMethod(name, request.getClass());

            Message result = (Message) method.invoke(instance, request);
            return (result == null) ? null : result.toByteArray();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 
     * @param args
     * @throws IOException
     * @throws DescriptorValidationException
     */
    public static void main(String[] args) throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final String queueName = "runningRabbit";

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(1);
        channel.queueDeclare(queueName, false, false, false, null);

        RPCServer server = new RPCServer(channel, queueName)
                .init()
                .setInstance(new RunnerServiceImpl())
                .setProtocol(IRunnerService.class);
        server.consume();
    }

}
