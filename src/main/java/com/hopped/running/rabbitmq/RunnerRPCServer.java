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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.hopped.running.protobuf.RunnerProtos.AuthResponse;
import com.hopped.running.protobuf.RunnerProtos.RPC;
import com.hopped.running.protobuf.RunnerProtos.User;
import com.hopped.running.rabbitmq.rpc.IRunnerService;
import com.hopped.running.rabbitmq.rpc.RunnerServiceImpl;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer.Delivery;

/**
 * @author Dennis Hoppe (hoppe.dennis@ymail.com)
 * 
 */
public class RunnerRPCServer extends ARunnerRPCServer {

    final static Logger logger = LoggerFactory.getLogger(RunnerRPCServer.class);
    private Map<String, String> mapping;

    public RunnerRPCServer(Channel channel, String queueName)
            throws IOException, DescriptorValidationException {
        super(channel, queueName);

        FileDescriptorSet descriptorSet = FileDescriptorSet
                .parseFrom(
                new FileInputStream("src/resources/protobuf/Runner.desc"));

        mapping = new HashMap<String, String>();
        for (FileDescriptorProto fdp : descriptorSet.getFileList()) {
            FileDescriptor fd = FileDescriptor.buildFrom(fdp,
                    new FileDescriptor[] {});

            for (Descriptor descriptor : fd.getMessageTypes()) {
                String className = fdp.getOptions().getJavaPackage() + "."
                        + fdp.getOptions().getJavaOuterClassname() + "$"
                        + descriptor.getName();
                mapping.put(descriptor.getFullName(), className);
            }
        }
    }

    public Object objectFromByteBuffer(byte[] buffer) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        byte[] name = new byte[bais.read()];
        bais.read(name); // TODO: Read fully??
        // Get the class name associated with the descriptor name
        logger.info("objectFrom: " + new String(name, "UTF-8"));
        String className = mapping.get(new String(name, "UTF-8"));
        Class<?> clazz = Thread.currentThread()
                .getContextClassLoader()
                .loadClass(className);
        Method parseFromMethod = clazz.getMethod("parseFrom", byte[].class);
        byte[] message = new byte[bais.read()];
        bais.read(message); // TODO: Read fully??
        return parseFromMethod.invoke(null, message);
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
            RPC rpcMessage = RPC.parseFrom(delivery.getBody());
            Method method = null;
            // TODO: don't do that!
            for (Method m : protocol.getMethods()) {
                if (m.getName().equals(rpcMessage.getMethod())) {
                    method = protocol.getMethod(rpcMessage.getMethod(),
                            m.getParameterTypes());
                }
            }
            // TODO: handle nullpointer exception

            byte[] payload = rpcMessage.getPayload().toByteArray();
            Object o = objectFromByteBuffer(payload);
            logger.info("object: " + o.toString());

            Message message = (Message) method.invoke(instance, o);
            byte[] name = message.getDescriptorForType().getFullName()
                    .getBytes("UTF-8");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(name.length); // TODO: length as int and not byte
            baos.write(name);
            byte[] messageBytes = message.toByteArray();
            baos.write(messageBytes.length); // TODO: Length as int and not byte
            baos.write(messageBytes);
            baos.flush();

            return baos.toByteArray();

        } catch (InvalidProtocolBufferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return AuthResponse.newBuilder()
                .setUser(User.newBuilder().setAlias("Dennis").build()).build()
                .toByteArray();
    }

    /**
     * 
     * @param args
     * @throws IOException
     * @throws DescriptorValidationException
     */
    public static void main(String[] args) throws IOException,
            DescriptorValidationException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final String queueName = "runningRabbit";

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.basicQos(1);
        channel.queueDeclare(queueName, false, false, false, null);

        ARunnerRPCServer server = new RunnerRPCServer(channel, queueName)
                .init()
                .setInstance(new RunnerServiceImpl())
                .setProtocol(IRunnerService.class);
        server.consume();
    }
}
