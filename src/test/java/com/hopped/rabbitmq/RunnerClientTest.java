/*
 * Copyright (C) 2014 Dennis Hoppe. All Rights Reserved.
 */
package com.hopped.rabbitmq;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Matchers.isNull;

import com.rabbitmq.client.ConnectionFactory;
import com.hopped.rabbitmq.RunnerClient;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Dennis Hoppe <hoppe.dennis@ymail.com>
 * @version $Id:$
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(RunnerClient.class)
public class RunnerClientTest {

    /* Passing parameters to the constructor RunnerClient */

    @Test
    public void testRunnerClientWithValidArguments() throws Throwable {
        // given
        RunnerClient client = mock(RunnerClient.class);
        ConnectionFactory factory = mock(ConnectionFactory.class);

        // when
        whenNew(RunnerClient.class)
            .withArguments(factory, "any string")
            .thenReturn(client);

        // verify
        client = new RunnerClient(factory, "any string");
        verifyNew(RunnerClient.class)
            .withArguments(factory, "any string");
    }

    /* Testing the first parameter (factory) */

    @Test(expected = IllegalArgumentException.class)
    public void testRunnerClientWithConnectionFactoryIsNull() throws Throwable {
        // given
        RunnerClient client = mock(RunnerClient.class);

        // when
        whenNew(RunnerClient.class)
            .withArguments(isNull(Object.class), isNull(String.class))
            .thenThrow(new IllegalArgumentException());

        // verify
        client = new RunnerClient(null, null);
        fail("IllegalArgumentException was expected but not thrown.");
    }

    /* Testing the second parameter (aRequestQueueName) */

    @Test(expected = IllegalArgumentException.class)
    public void testRunnerClientWithRequestQueueNameIsNull() throws Throwable {
        // given
        RunnerClient client = mock(RunnerClient.class);
        ConnectionFactory factory = mock(ConnectionFactory.class);

        // when
        whenNew(RunnerClient.class)
            .withArguments(same(factory), isNull(String.class))
            .thenThrow(new IllegalArgumentException());

        // verify
        client = new RunnerClient(factory, null);
        fail("IllegalArgumentException was expected but not thrown.");
    }

    @Test(expected = IllegalStateException.class)
    public void testRunnerClientWithRequestQueueNameIsEmpty() throws Throwable {
        // given
        RunnerClient client = mock(RunnerClient.class);
        ConnectionFactory factory = mock(ConnectionFactory.class);

        // when
        whenNew(RunnerClient.class)
            .withArguments(same(factory), eq(new String()))
            .thenThrow(new IllegalStateException());

        // verify
        client = new RunnerClient(factory, new String());
        fail("IllegalStateException was expected but not thrown.");
    }
}
