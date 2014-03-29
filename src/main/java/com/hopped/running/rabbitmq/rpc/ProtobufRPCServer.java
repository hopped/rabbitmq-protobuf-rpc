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

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Message;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer.Delivery;

/**
 * @author Dennis Hoppe (hoppe.dennis@ymail.com)
 * 
 */
public class ProtobufRPCServer extends ARPCServer<ProtobufRPCServer> {

    private final static Logger logger = LoggerFactory
            .getLogger(ProtobufRPCServer.class);

    /**
     * 
     * @param channel
     * @param queueName
     */
    public ProtobufRPCServer(Channel channel, String queueName) {
        super(channel, queueName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.hopped.running.rabbitmq.rpc.ARPCServer#self()
     */
    @Override
    public ProtobufRPCServer self() {
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
            RPCMessage invoker = RPCMessage.parseFrom(delivery.getBody());
            String name = invoker.getMethod();
            Message request = (Message) invoker.getRequestObject();
            Method method = protocol.getMethod(name, request.getClass());

            Message result = (Message) method.invoke(instance, request);
            return (result == null) ? null : result.toByteArray();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

}
