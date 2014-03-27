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
package com.hopped.running.demo;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hopped.running.protobuf.RunnerProtos.AuthRequest;
import com.hopped.running.protobuf.RunnerProtos.AuthResponse;
import com.hopped.running.protobuf.RunnerProtos.Error;
import com.hopped.running.protobuf.RunnerProtos.User;
import com.hopped.running.rabbitmq.RunnerClient;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 */
public class Demo {

    final static Logger logger = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) throws Exception {
        logger.info("RabbitMQ RPC example (Java <> Perl)");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final String requestQueueName = "runningRabbit";

        /* AUTHENTICATION */

        RunnerClient client = new RunnerClient(factory, requestQueueName);
        try {
            client.init();
        } catch (IOException ioe) {
            logger.error(ioe.getMessage());
            return;
        }

        AuthRequest authRequest = AuthRequest.newBuilder()
            .setUsername("hopped")
            .setPassword("is_fantastic")
            .build();
        logger.info("Logging in 'hoppe' ...");

        AuthResponse response = client.getResponse(authRequest);
        Error error = response.getError();
        if (error.getErrorCode() > 0) {
            logger.error(error.getErrorMessage());
        } else {
            User user = response.getUser();
            logger.info("  User 'hoppe' logged in.");
            logger.info("  Hello " + user.getFirstName());
        }

        /* TODO: GET LIST OF RUNS */



        client.close();
        logger.info("Done.");
    }
}
