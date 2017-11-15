/**
 * This file is part of core.
 *
 * core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with core.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hobbit.core.components;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.TestConstants;
import org.hobbit.core.components.dummy.DummyComponentExecutor;
import org.hobbit.core.components.test.InMemoryEvaluationStore;
import org.hobbit.core.components.test.InMemoryEvaluationStore.ResultImpl;
import org.hobbit.core.components.test.InMemoryEvaluationStore.ResultPairImpl;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the workflow of the {@link AbstractTaskGenerator} class and the
 * communication between the {@link AbstractDataGenerator},
 * {@link AbstractSystemAdapter}, {@link AbstractEvaluationStorage} and
 * {@link AbstractTaskGenerator} classes. Note that this test needs a running
 * RabbitMQ instance. Its host name can be set using the
 * {@link #RABBIT_HOST_NAME} parameter.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class EvaluationModuleTest extends AbstractEvaluationModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationModuleTest.class);

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private Map<String, ResultPairImpl> expectedResults = new HashMap<>();
    private int numberOfMessages = 30000;
    private Set<String> receivedResults = Collections.synchronizedSet(new HashSet<>());
    private Semaphore evalStoreReady = new Semaphore(0);

    @Test(timeout = 60000)
    public void test() throws Exception {
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, TestConstants.RABBIT_HOST);
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, "0");
        environmentVariables.set(Constants.HOBBIT_EXPERIMENT_URI_KEY, Constants.EXPERIMENT_URI_NS + "123");

        // Create the eval store and add some data
        InMemoryEvaluationStore evalStore = new InMemoryEvaluationStore();
        Random rand = new Random();

        String taskId;
        ResultImpl expResult, sysResult;
        ResultPairImpl pair;
        boolean expResultMissing, sysResultMissing;
        for (int i = 0; i < numberOfMessages; ++i) {
            taskId = Integer.toString(i);
            expResultMissing = rand.nextDouble() > 0.9;
            sysResultMissing = !expResultMissing && (rand.nextDouble() > 0.9);
            pair = new ResultPairImpl();

            if (!expResultMissing) {
                expResult = new ResultImpl(rand.nextLong(), RabbitMQUtils.writeString(taskId));
                evalStore.putResult(true, taskId, expResult.getSentTimestamp(), expResult.getData());
                pair.setExpected(expResult);
            }

            if (!sysResultMissing) {
                sysResult = new ResultImpl(rand.nextLong(), expResultMissing ? RabbitMQUtils.writeString(taskId)
                        : RabbitMQUtils.writeString(Integer.toString(rand.nextInt())));
                evalStore.putResult(false, taskId, sysResult.getSentTimestamp(), sysResult.getData());
                pair.setActual(sysResult);
            }

            expectedResults.put(taskId, pair);
        }
        DummyComponentExecutor evalStoreExecutor = new DummyComponentExecutor(evalStore);
        Thread evalStoreThread = new Thread(evalStoreExecutor);
        evalStoreThread.start();

        init();
        evalStoreReady.acquire();

        try {

            run();
            sendToCmdQueue(Commands.EVAL_STORAGE_TERMINATE);

            evalStoreThread.join();

            Assert.assertTrue(evalStoreExecutor.isSuccess());

            String expectedTaskIds[] = expectedResults.keySet().toArray(new String[expectedResults.size()]);
            Arrays.sort(expectedTaskIds);
            String receivedTaskIds[] = receivedResults.toArray(new String[receivedResults.size()]);
            Arrays.sort(receivedTaskIds);
            Assert.assertArrayEquals(expectedTaskIds, receivedTaskIds);
        } finally {
            close();
        }
    }

    @Override
    protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp,
            long responseReceivedTimestamp) throws Exception {
        Assert.assertTrue((expectedData.length + receivedData.length) > 0);
        String taskId = expectedData.length > 0 ? RabbitMQUtils.readString(expectedData)
                : RabbitMQUtils.readString(receivedData);
        Assert.assertTrue(taskId + " is not known.", expectedResults.containsKey(taskId));
        ResultPairImpl pair = expectedResults.get(taskId);

        if (expectedData.length == 0) {
            Assert.assertNull(pair.getExpected());
            Assert.assertEquals(0, taskSentTimestamp);
        } else {
            Assert.assertNotNull(pair.getExpected());
            Assert.assertArrayEquals(pair.getExpected().getData(), expectedData);
            Assert.assertEquals(pair.getExpected().getSentTimestamp(), taskSentTimestamp);
        }

        if (receivedData.length == 0) {
            Assert.assertNull(pair.getActual());
            Assert.assertEquals(0, responseReceivedTimestamp);
        } else {
            Assert.assertNotNull(pair.getActual());
            Assert.assertArrayEquals(pair.getActual().getData(), receivedData);
            Assert.assertEquals(pair.getActual().getSentTimestamp(), responseReceivedTimestamp);
        }

        receivedResults.add(taskId);
    }

    @Override
    protected Model summarizeEvaluation() throws Exception {
        return ModelFactory.createDefaultModel();
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        LOGGER.info("received command {}", Commands.toString(command));
        if (command == Commands.EVAL_STORAGE_READY_SIGNAL) {
            evalStoreReady.release();
        }
        super.receiveCommand(command, data);
    }
}
