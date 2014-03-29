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

import com.hopped.running.protobuf.RunnerProtos.Ack;
import com.hopped.running.protobuf.RunnerProtos.AuthRequest;
import com.hopped.running.protobuf.RunnerProtos.AuthResponse;
import com.hopped.running.protobuf.RunnerProtos.User;

/**
 * @author Dennis Hoppe (hoppe.dennis@ymail.com)
 */
public class RunnerServiceImpl implements IRunnerService {

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.hopped.running.rabbitmq.rpc.RunnerService#login(com.hopped.running
     * .protobuf.RunnerProtos.AuthRequest)
     */
    @Override
    public AuthResponse login(AuthRequest request) {
        // TODO: remove dummy implementation
        return AuthResponse.newBuilder().setSessionId("1234").build();
    }

    @Override
    public Ack setProfile(User user) {
        System.out.println("server::setProfile called " + user.getAlias());
        return Ack.newBuilder().setSuccess(true).build();
    }
}
