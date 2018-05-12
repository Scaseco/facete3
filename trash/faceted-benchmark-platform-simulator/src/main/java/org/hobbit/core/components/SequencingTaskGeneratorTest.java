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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.TestConstants;
import org.hobbit.core.components.dummy.DummyComponentExecutor;
import org.hobbit.core.components.dummy.DummyDataCreator;
import org.hobbit.core.components.dummy.DummyEvalStoreReceiver;
import org.hobbit.core.components.dummy.DummySystem;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests the workflow of the {@link AbstractSequencingTaskGenerator} class and
 * the communication between the {@link AbstractDataGenerator},
 * {@link AbstractSystemAdapter}, {@link AbstractEvaluationStorage} and
 * {@link AbstractSequencingTaskGenerator} classes. Note that this test needs a
 * running RabbitMQ instance. Its host name can be set using the
 * {@link #RABBIT_HOST_NAME} parameter.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
@RunWith(Parameterized.class)
public class SequencingTaskGeneratorTest extends AbstractSequencingTaskGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SequencingTaskGeneratorTest.class);

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        // We use only one single data generator without parallel message
        // processing
        testConfigs.add(new Object[] { 1, 5000, 1 });
        // We use only one single data generator with parallel message
        // processing (max 100)
//        testConfigs.add(new Object[] { 1, 5000, 100 });
        // We use two data generators without parallel message processing
        testConfigs.add(new Object[] { 2, 5000, 1 });
        // We use two data generators with parallel message processing (max 100)
//        testConfigs.add(new Object[] { 2, 5000, 100 });
        // We use ten data generators without parallel message processing
        testConfigs.add(new Object[] { 10, 500, 1 });
        // We use ten data generators with parallel message processing (max 100)
//        testConfigs.add(new Object[] { 10, 500, 100 });
        return testConfigs;
    }

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private List<String> sentTasks = Collections.synchronizedList(new ArrayList<String>());
    private List<String> expectedResponses = Collections.synchronizedList(new ArrayList<String>());
    private int terminationCount = 0;
    private int numberOfGenerators;
    private int numberOfMessages;
    private Semaphore dataGensReady = new Semaphore(0);
    private Semaphore systemReady = new Semaphore(0);
    private Semaphore evalStoreReady = new Semaphore(0);

    public SequencingTaskGeneratorTest(int numberOfGenerators, int numberOfMessages, int numberOfMessagesInParallel) {
        // TODO add me super(numberOfMessagesInParallel);
        this.numberOfGenerators = numberOfGenerators;
        this.numberOfMessages = numberOfMessages;
    }

    @Test(timeout = 60000)
    public void test() throws Exception {
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, TestConstants.RABBIT_HOST);
        environmentVariables.set(Constants.GENERATOR_ID_KEY, "0");
        environmentVariables.set(Constants.GENERATOR_COUNT_KEY, "1");
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, "0");

        // Set the acknowledgement flag to true (read by the evaluation storage)
        environmentVariables.set(Constants.ACKNOWLEDGEMENT_FLAG_KEY, "true");

        init();

        Thread[] dataGenThreads = new Thread[numberOfGenerators];
        DummyComponentExecutor[] dataGenExecutors = new DummyComponentExecutor[numberOfGenerators];
        for (int i = 0; i < dataGenThreads.length; ++i) {
            DummyDataCreator dataGenerator = new DummyDataCreator(numberOfMessages);
            dataGenExecutors[i] = new DummyComponentExecutor(dataGenerator) {
                @Override
                public void run() {
                    super.run();
                    dataGeneratorTerminated();
                }
            };
            dataGenThreads[i] = new Thread(dataGenExecutors[i]);
            dataGenThreads[i].start();
        }

        DummySystem system = new DummySystem();
        DummyComponentExecutor systemExecutor = new DummyComponentExecutor(system);
        Thread systemThread = new Thread(systemExecutor);
        systemThread.start();

        DummyEvalStoreReceiver evalStore = new DummyEvalStoreReceiver();
        DummyComponentExecutor evalStoreExecutor = new DummyComponentExecutor(evalStore);
        Thread evalStoreThread = new Thread(evalStoreExecutor);
        evalStoreThread.start();

        dataGensReady.acquire(numberOfGenerators);
        systemReady.acquire();
        evalStoreReady.acquire();

        try {
            // start dummy
            sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
            sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);

            run();
            sendToCmdQueue(Commands.TASK_GENERATION_FINISHED);
            sendToCmdQueue(Commands.EVAL_STORAGE_TERMINATE);

            for (int i = 0; i < dataGenThreads.length; ++i) {
                dataGenThreads[i].join();
            }
            systemThread.join();
            evalStoreThread.join();

            for (int i = 0; i < dataGenExecutors.length; ++i) {
                Assert.assertTrue(dataGenExecutors[i].isSuccess());
            }
            Assert.assertTrue(systemExecutor.isSuccess());
            Assert.assertTrue(evalStoreExecutor.isSuccess());

            Collections.sort(sentTasks);
            List<String> receivedData = system.getReceivedtasks();
            Collections.sort(receivedData);
            Assert.assertArrayEquals(sentTasks.toArray(new String[sentTasks.size()]),
                    receivedData.toArray(new String[receivedData.size()]));
            Assert.assertEquals(numberOfGenerators * numberOfMessages, sentTasks.size());
            receivedData = evalStore.getExpectedResponses();
            Collections.sort(receivedData);
            Collections.sort(expectedResponses);
            Assert.assertArrayEquals(expectedResponses.toArray(new String[expectedResponses.size()]),
                    receivedData.toArray(new String[receivedData.size()]));
            Assert.assertEquals(numberOfGenerators * numberOfMessages, sentTasks.size());
        } finally {
            close();
        }
    }

    @Override
    protected void generateTask(byte[] data) throws Exception {
        String taskIdString = getNextTaskId();
        sendTaskToSystemAdapterInSequence(taskIdString, data);
        long timestamp = System.currentTimeMillis();
        String dataString = RabbitMQUtils.readString(data);
        StringBuilder builder = new StringBuilder();
        builder.append(taskIdString);
        builder.append(dataString);
        sentTasks.add(builder.toString());

        sendTaskToEvalStorage(taskIdString, timestamp, data);
        builder.delete(0, builder.length());
        builder.append(taskIdString);
        builder.append(Long.toString(timestamp));
        builder.append(dataString);
        expectedResponses.add(builder.toString());
    }

    protected synchronized void dataGeneratorTerminated() {
        ++terminationCount;
        if (terminationCount == numberOfGenerators) {
            try {
                sendToCmdQueue(Commands.DATA_GENERATION_FINISHED);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        LOGGER.info("received command {}", Commands.toString(command));
        if (command == Commands.DATA_GENERATOR_READY_SIGNAL) {
            dataGensReady.release();
        }
        if (command == Commands.SYSTEM_READY_SIGNAL) {
            systemReady.release();
        }
        if (command == Commands.EVAL_STORAGE_READY_SIGNAL) {
            evalStoreReady.release();
        }
        super.receiveCommand(command, data);
    }

}
