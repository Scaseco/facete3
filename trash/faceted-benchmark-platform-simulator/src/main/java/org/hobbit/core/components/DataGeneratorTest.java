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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.TestConstants;
import org.hobbit.core.components.dummy.DummyComponentExecutor;
import org.hobbit.core.components.dummy.DummySystemReceiver;
import org.hobbit.core.components.dummy.DummyTaskGenReceiver;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the workflow of the {@link AbstractDataGenerator} class and the
 * communication between the {@link AbstractDataGenerator},
 * {@link AbstractSystemAdapter} and {@link AbstractTaskGenerator} classes. Note
 * that this test needs a running RabbitMQ instance. Its host name can be set
 * using the {@link #RABBIT_HOST_NAME} parameter.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
@RunWith(Parameterized.class)
public class DataGeneratorTest extends AbstractDataGenerator {

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        // We use only one single data generator
        testConfigs.add(new Object[] { 1, 10000 });
        // We use two data generators
        testConfigs.add(new Object[] { 2, 10000 });
        // We use ten data generators
        testConfigs.add(new Object[] { 10, 20000 });
        return testConfigs;
    }

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private List<String> sentData = new ArrayList<String>();
    private int numberOfGenerators;
    private int numberOfMessages;
    private Semaphore taskGensReady = new Semaphore(0);
    private Semaphore systemReady = new Semaphore(0);

    public DataGeneratorTest(int numberOfGenerators, int numberOfMessages) {
        this.numberOfGenerators = numberOfGenerators;
        this.numberOfMessages = numberOfMessages;
    }

    @Override
    protected void generateData() throws Exception {
        byte data[];
        String msg;
        for (int i = 0; i < numberOfMessages; ++i) {
            msg = Integer.toString(i);
            sentData.add(msg);
            data = RabbitMQUtils.writeString(msg);
            sendDataToSystemAdapter(data);
            sendDataToTaskGenerator(data);
        }
    }

    @Test(timeout=30000)
    public void test() throws Exception {
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, TestConstants.RABBIT_HOST);
        environmentVariables.set(Constants.GENERATOR_ID_KEY, "0");
        environmentVariables.set(Constants.GENERATOR_COUNT_KEY, "1");
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, "0");

        init();

        DummySystemReceiver system = new DummySystemReceiver();
        DummyComponentExecutor systemExecutor = new DummyComponentExecutor(system);
        Thread systemThread = new Thread(systemExecutor);
        systemThread.start();

        DummyTaskGenReceiver[] taskGens = new DummyTaskGenReceiver[numberOfGenerators];
        DummyComponentExecutor[] taskGenExecutors = new DummyComponentExecutor[numberOfGenerators];
        Thread[] taskGenThreads = new Thread[numberOfGenerators];
        for (int i = 0; i < taskGenThreads.length; ++i) {
            taskGens[i] = new DummyTaskGenReceiver();
            taskGenExecutors[i] = new DummyComponentExecutor(taskGens[i]);
            taskGenThreads[i] = new Thread(taskGenExecutors[i]);
            taskGenThreads[i].start();
        }

        systemReady.acquire();
        taskGensReady.acquire(numberOfGenerators);

        try {
            // start dummy
            sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
            sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);
            run();
            sendToCmdQueue(Commands.DATA_GENERATION_FINISHED);
            sendToCmdQueue(Commands.TASK_GENERATION_FINISHED);

            systemThread.join();
            for (int i = 0; i < taskGenThreads.length; ++i) {
                taskGenThreads[i].join();
            }

            Assert.assertTrue(systemExecutor.isSuccess());
            for (int i = 0; i < taskGenExecutors.length; ++i) {
                Assert.assertTrue(taskGenExecutors[i].isSuccess());
            }

            Collections.sort(sentData);
            List<String> receivedData = system.getReceiveddata();
            Collections.sort(receivedData);
            Assert.assertArrayEquals(sentData.toArray(new String[sentData.size()]),
                    receivedData.toArray(new String[receivedData.size()]));
            // collect the data from all task generators
            receivedData = new ArrayList<String>();
            for (int i = 0; i < taskGens.length; ++i) {
                receivedData.addAll(taskGens[i].getReceiveddata());
            }
            Collections.sort(receivedData);
            Assert.assertArrayEquals(sentData.toArray(new String[sentData.size()]),
                    receivedData.toArray(new String[receivedData.size()]));
        } finally {
            close();
        }
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        if(command == Commands.TASK_GENERATOR_READY_SIGNAL) {
            taskGensReady.release();
        }
        if (command == Commands.SYSTEM_READY_SIGNAL) {
            systemReady.release();
        }
        super.receiveCommand(command, data);
    }
}
