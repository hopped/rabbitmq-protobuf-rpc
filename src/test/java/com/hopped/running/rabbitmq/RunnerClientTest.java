/*
 * Copyright (C) 2014 Dennis Hoppe. All Rights Reserved.
 */
package com.hopped.running.rabbitmq;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Matchers.isNull;

import com.rabbitmq.client.ConnectionFactory;
import com.hopped.running.rabbitmq.RunnerClient;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 * @version $Id:$
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
