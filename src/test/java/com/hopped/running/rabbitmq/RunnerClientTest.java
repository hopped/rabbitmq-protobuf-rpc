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

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rabbitmq.client.ConnectionFactory;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class RunnerClientTest {

    @Mock
    private ConnectionFactory factory;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    /* Passing parameters to the constructor RunnerClient */

    @Test
    public void testRunnerClientWithValidArguments() throws Throwable {
        // when
        RunnerClient client = new RunnerClient(factory, "queue");

        // verify
        assertEquals(factory, client.getConnectionFactory());
        assertEquals("queue", client.getRequestQueueName());
    }

    /* Testing the first parameter (factory) */

    @Test
    public void testRunnerClientWithConnectionFactoryIsNull() throws Throwable {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("factory is null");

        // when
        new RunnerClient(null, null);
    }

    /* Testing the second parameter (aRequestQueueName) */

    @Test
    public void testRunnerClientWithRequestQueueNameIsNull() throws Throwable {
        // expect
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("requestQueueName is null");

        // when
        new RunnerClient(factory, null);
    }

    @Test
    public void testRunnerClientWithRequestQueueNameIsEmpty() throws Throwable {
        // expect
        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("requestQueueName is empty");

        // when
        new RunnerClient(factory, "");
    }
}
