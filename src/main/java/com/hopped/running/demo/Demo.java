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

import com.hopped.running.protobuf.RunnerProtos.Ack;
import com.hopped.running.protobuf.RunnerProtos.AuthRequest;
import com.hopped.running.protobuf.RunnerProtos.AuthResponse;
import com.hopped.running.protobuf.RunnerProtos.User;
import com.hopped.running.rabbitmq.RunnerClient;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 */
public class Demo {

    final static Logger logger = LoggerFactory.getLogger(Demo.class);

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
