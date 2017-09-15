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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.TestConstants;
import org.hobbit.core.components.dummy.DummyComponentExecutor;
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

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

@RunWith(Parameterized.class)
public class BenchmarkControllerTest extends AbstractBenchmarkController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BenchmarkControllerTest.class);

    private static final String HOBBIT_SESSION_ID = "123";
    private static final String SYSTEM_CONTAINER_ID = "systemContainerId";
    private static final String DATA_GEN_IMAGE = "datagenimage";
    private static final String TASK_GEN_IMAGE = "taskgenimage";
    private static final String EVAL_IMAGE = "evaluationimage";

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        testConfigs.add(new Object[] { 1, 1 });
        testConfigs.add(new Object[] { 1, 10 });
        testConfigs.add(new Object[] { 10, 1 });
        testConfigs.add(new Object[] { 10, 10 });
        return testConfigs;
    }

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private int numberOfDataGenerators;
    private int numberOfTaskGenerators;
    private String sessionId;

    public BenchmarkControllerTest(int numberOfDataGenerators, int numberOfTaskGenerators) {
        this.numberOfDataGenerators = numberOfDataGenerators;
        this.numberOfTaskGenerators = numberOfTaskGenerators;
        this.sessionId = HOBBIT_SESSION_ID + Integer.toString(numberOfDataGenerators)
                + Integer.toString(numberOfTaskGenerators);
    }

    @Test
    public void test() throws Exception {
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, TestConstants.RABBIT_HOST);
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, sessionId);
        environmentVariables.set(Constants.BENCHMARK_PARAMETERS_MODEL_KEY,
                "{ \"@id\" : \"http://w3id.org/hobbit/experiments#New\", \"@type\" : \"http://w3id.org/hobbit/vocab#Experiment\" }");
        environmentVariables.set(Constants.HOBBIT_EXPERIMENT_URI_KEY, Constants.EXPERIMENT_URI_NS + sessionId);
        // Needed for the generators
        environmentVariables.set(Constants.GENERATOR_ID_KEY, "0");
        environmentVariables.set(Constants.GENERATOR_COUNT_KEY, "1");

        final DummyPlatformController dummyPlatformController = new DummyPlatformController(sessionId);
        try {
            DummyComponentExecutor dummyPlatformExecutor = new DummyComponentExecutor(dummyPlatformController);
            Thread dummyPlatformThread = new Thread(dummyPlatformExecutor);
            dummyPlatformThread.start();
            dummyPlatformController.waitForControllerBeingReady();

            AbstractBenchmarkController controller = this;
            DummyComponentExecutor controllerExecutor = new DummyComponentExecutor(controller);
            Thread controllerThread = new Thread(controllerExecutor);
            controllerThread.start();
            // wait for the benchmark controller to start

            Thread.sleep(10000);
            dummyPlatformController.sendToCmdQueue(Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS,
                    Commands.DOCKER_CONTAINER_TERMINATED, RabbitMQUtils.writeByteArrays(null,
                            new byte[][] { RabbitMQUtils.writeString(SYSTEM_CONTAINER_ID) }, new byte[] { (byte) 0 }),
                    null);
            Thread.sleep(10000);

            for (Thread t : dummyPlatformController.dataGenThreads) {
                t.join(10000);
                Assert.assertFalse(t.isAlive());
            }
            for (Thread t : dummyPlatformController.taskGenThreads) {
                t.join(10000);
                Assert.assertFalse(t.isAlive());
            }

            for (DummyComponentExecutor executor : dummyPlatformController.dataGenExecutors) {
                Assert.assertTrue(executor.isSuccess());
            }
            for (DummyComponentExecutor executor : dummyPlatformController.taskGenExecutors) {
                Assert.assertTrue(executor.isSuccess());
            }

            // Make sure that the benchmark controller terminates during the
            // next seconds
            controllerThread.join(5000);
            Assert.assertFalse(controllerThread.isAlive());
        } finally {
            dummyPlatformController.terminate();
            for (DummyComponentExecutor executor : dummyPlatformController.dataGenExecutors) {
                try {
                    IOUtils.closeQuietly(executor.getComponent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (DummyComponentExecutor executor : dummyPlatformController.taskGenExecutors) {
                try {
                    IOUtils.closeQuietly(executor.getComponent());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            close();
        }
    }

    @Override
    public void init() throws Exception {
        super.init();

        // create data generators
        createDataGenerators(DATA_GEN_IMAGE, numberOfDataGenerators, null);

        // Create task generators
        createTaskGenerators(TASK_GEN_IMAGE, numberOfTaskGenerators, null);

        // Create evaluation storage
        createEvaluationStorage(EVAL_IMAGE, null);

        // Wait for all components to finish their initialization
        waitForComponentsToInitialize();
    }

    @Override
    protected void executeBenchmark() throws Exception {
        // give the start signals
        sendToCmdQueue(Commands.TASK_GENERATOR_START_SIGNAL);
        sendToCmdQueue(Commands.DATA_GENERATOR_START_SIGNAL);

        // wait for the data generators to finish their work
        waitForDataGenToFinish();

        // wait for the task generators to finish their work
        waitForTaskGenToFinish();

        // wait for the system to terminate
        waitForSystemToFinish();

        // Create the evaluation module

        // wait for the evaluation to finish
        // waitForEvalComponentsToFinish();

        // the evaluation module should have sent an RDF model containing the
        // results. We should add the configuration of the benchmark to this
        // model.
        // this.resultModel.add(...);

        // Send the resultModul to the platform controller and terminate
        sendResultModel(ModelFactory.createDefaultModel());
    }

    protected static class DummyPlatformController extends AbstractCommandReceivingComponent {

        public List<DummyComponentExecutor> dataGenExecutors = new ArrayList<DummyComponentExecutor>();
        public List<Thread> dataGenThreads = new ArrayList<>();
        public List<DummyComponentExecutor> taskGenExecutors = new ArrayList<DummyComponentExecutor>();
        public List<Thread> taskGenThreads = new ArrayList<>();
        public Random random = new Random();
        private boolean readyFlag = false;

        private String sessionId;
        private Semaphore terminationMutex = new Semaphore(0);

        public DummyPlatformController(String sessionId) {
            super();
            this.sessionId = sessionId;
        }

        protected void handleCmd(byte bytes[], String replyTo) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            int idLength = buffer.getInt();
            byte sessionIdBytes[] = new byte[idLength];
            buffer.get(sessionIdBytes);
            String sessionId = new String(sessionIdBytes, Charsets.UTF_8);
            byte command = buffer.get();
            byte remainingData[];
            if (buffer.remaining() > 0) {
                remainingData = new byte[buffer.remaining()];
                buffer.get(remainingData);
            } else {
                remainingData = new byte[0];
            }
            receiveCommand(command, remainingData, sessionId, replyTo);
        }

        public void receiveCommand(byte command, byte[] data, String sessionId, String replyTo) {
            LOGGER.info("received command: session={}, command={}, data={}", sessionId, Commands.toString(command),
                    data != null ? RabbitMQUtils.readString(data) : "null");
            if (command == Commands.BENCHMARK_READY_SIGNAL) {
                System.out.println("Benchmark Ready!");
                try {
                    sendToCmdQueue(sessionId, Commands.START_BENCHMARK_SIGNAL,
                            RabbitMQUtils.writeString(SYSTEM_CONTAINER_ID), null);
                } catch (IOException e) {
                    e.printStackTrace();
                    Assert.fail(e.getLocalizedMessage());
                }
            } else if (command == Commands.DOCKER_CONTAINER_START) {
                try {
                    String startCommandJson = RabbitMQUtils.readString(data);
                    final String containerId = Integer.toString(random.nextInt());

                    if (startCommandJson.contains(DATA_GEN_IMAGE)) {
                        // Create data generators that are waiting for a random
                        // amount of time and terminate after that
                        DummyComponentExecutor dataGenExecutor = new DummyComponentExecutor(
                                new AbstractDataGenerator() {
                                    @Override
                                    protected void generateData() throws Exception {
                                        LOGGER.debug("Data Generator started...");
                                        Thread.sleep(1000 + random.nextInt(1000));
                                    }
                                }) {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    sendToCmdQueue(Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS,
                                            Commands.DOCKER_CONTAINER_TERMINATED,
                                            RabbitMQUtils.writeByteArrays(null,
                                                    new byte[][] { RabbitMQUtils.writeString(containerId) },
                                                    new byte[] { (byte) 0 }),
                                            null);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    success = false;
                                }
                            }
                        };
                        dataGenExecutors.add(dataGenExecutor);
                        Thread t = new Thread(dataGenExecutor);
                        dataGenThreads.add(t);
                        t.start();
                        cmdChannel.basicPublish("", replyTo, MessageProperties.PERSISTENT_BASIC,
                                RabbitMQUtils.writeString(containerId));
                    } else if (startCommandJson.contains(TASK_GEN_IMAGE)) {
                        // Create task generators that are waiting for a random
                        // amount of
                        // time and terminate after that
                        DummyComponentExecutor taskGenExecutor = new DummyComponentExecutor(
                                new AbstractTaskGenerator() {
                                    @Override
                                    public void run() throws Exception {
                                        LOGGER.debug("Task Generator started...");
                                        super.run();
                                    }

                                    @Override
                                    protected void generateTask(byte[] data) throws Exception {
                                    }
                                }) {
                            @Override
                            public void run() {
                                super.run();
                                try {
                                    sendToCmdQueue(Constants.HOBBIT_SESSION_ID_FOR_BROADCASTS,
                                            Commands.DOCKER_CONTAINER_TERMINATED,
                                            RabbitMQUtils.writeByteArrays(null,
                                                    new byte[][] { RabbitMQUtils.writeString(containerId) },
                                                    new byte[] { (byte) 0 }),
                                            null);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    success = false;
                                }
                            }
                        };
                        taskGenExecutors.add(taskGenExecutor);
                        Thread t = new Thread(taskGenExecutor);
                        taskGenThreads.add(t);
                        t.start();
                        cmdChannel.basicPublish("", replyTo, MessageProperties.PERSISTENT_BASIC,
                                RabbitMQUtils.writeString(containerId));
                    } else if (startCommandJson.contains(EVAL_IMAGE)) {
                        cmdChannel.basicPublish("", replyTo, MessageProperties.PERSISTENT_BASIC,
                                RabbitMQUtils.writeString(containerId));
                        sendToCmdQueue(this.sessionId, Commands.EVAL_STORAGE_READY_SIGNAL, null, null);
                    } else {
                        LOGGER.error("Got unknown start command. Ignoring it.");
                    }
                } catch (IOException e) {
                    LOGGER.error("Exception while trying to respond to a container creation command.", e);
                }
            }
        }

        public void waitForControllerBeingReady() throws InterruptedException {
            while (!readyFlag) {
                Thread.sleep(500);
            }
        }

        public void sendToCmdQueue(String address, byte command, byte data[], BasicProperties props)
                throws IOException {
            byte sessionIdBytes[] = RabbitMQUtils.writeString(address);
            // + 5 because 4 bytes for the session ID length and 1 byte for the
            // command
            int dataLength = sessionIdBytes.length + 5;
            boolean attachData = (data != null) && (data.length > 0);
            if (attachData) {
                dataLength += data.length;
            }
            ByteBuffer buffer = ByteBuffer.allocate(dataLength);
            buffer.putInt(sessionIdBytes.length);
            buffer.put(sessionIdBytes);
            buffer.put(command);
            if (attachData) {
                buffer.put(data);
            }
            cmdChannel.basicPublish(Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "", props, buffer.array());
        }

        @Override
        public void receiveCommand(byte command, byte[] data) {
        }

        @Override
        public void run() throws Exception {
            readyFlag = true;
            terminationMutex.acquire();
        }

        public void terminate() {
            terminationMutex.release();
        }

    }
}
