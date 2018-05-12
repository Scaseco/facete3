package org.hobbit.core.mimic;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.TestConstants;
import org.hobbit.core.components.AbstractCommandReceivingComponent;
import org.hobbit.core.components.AbstractComponent;
import org.hobbit.core.components.AbstractPlatformConnectorComponent;
import org.hobbit.core.components.dummy.DummyComponentExecutor;
import org.hobbit.core.data.StartCommandData;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.rabbit.SimpleFileSender;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.MessageProperties;

/**
 * This is a test that simulates the workflow of the
 * {@link DockerBasedMimickingAlg} class simulating the platform controller that
 * would normally create a docker container.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class DockerBasedMimickingAlgTest extends AbstractPlatformConnectorComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerBasedMimickingAlgTest.class);

    private static final String HOBBIT_SESSION_ID = "123";
    private static final String MIMICKING_ALGORITHM_DOCKER_IMAGE = "mimickingAlg";
    private static final int DATA_SIZE = 1000;
    private static final String DATA_SIZE_KEY = "DATA_SIZE";
    private static final String OUTPUT_FILE_NAME = "output.txt";

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    private File outputDir;

    @Test(timeout = 30000)
    public void test() throws Exception {
        environmentVariables.set(Constants.RABBIT_MQ_HOST_NAME_KEY, TestConstants.RABBIT_HOST);
        environmentVariables.set(Constants.HOBBIT_SESSION_ID_KEY, HOBBIT_SESSION_ID);

        outputDir = generateTempDir();
        LOGGER.debug("File will be writte to {}", outputDir.getAbsolutePath());
        // start platform controller
        LOGGER.debug("Creating controller and waiting for it to be ready...");
        DummyPlatformController platform = new DummyPlatformController(HOBBIT_SESSION_ID);
        DummyComponentExecutor platformExecutor = new DummyComponentExecutor(platform);
        Thread platformThread = new Thread(platformExecutor);
        platformThread.start();
        platform.waitForControllerBeingReady();
        // init this class
        LOGGER.debug("Initializing...");
        init();
        // run this class
        LOGGER.debug("Running...");
        run();
        // shutdown the platform
        LOGGER.debug("Shutting down the platform...");
        platform.terminate();
        platformThread.join();

        // check received data
        List<String> receivedLines = FileUtils
                .readLines(new File(outputDir.getAbsolutePath() + File.separatorChar + OUTPUT_FILE_NAME));
        Assert.assertEquals(DATA_SIZE, receivedLines.size());
        for (int i = 0; i < DATA_SIZE; ++i) {
            Assert.assertEquals(Integer.toString(i), receivedLines.get(i));
        }
    }

    @Override
    public void run() throws Exception {
        // start mimicking
        DockerBasedMimickingAlg algorithm = new DockerBasedMimickingAlg(this, MIMICKING_ALGORITHM_DOCKER_IMAGE);
        algorithm.generateData(outputDir.getAbsolutePath(),
                new String[] { DATA_SIZE_KEY + "=" + Integer.toString(DATA_SIZE) });
    }

    private File generateTempDir() throws IOException {
        File temp = File.createTempFile("test", "test");
        temp.delete();
        temp.mkdir();
        return temp;
    }

    protected static class DummyPlatformController extends AbstractCommandReceivingComponent {

        public DummyComponentExecutor mimickingExecutor;
        public Thread mimickingThread;
        public Random random = new Random();
        private boolean readyFlag = false;
        private Gson gson = new Gson();

        private Semaphore terminationMutex = new Semaphore(0);

        public DummyPlatformController(String sessionId) {
            super();
            addCommandHeaderId(sessionId);
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
            if (command == Commands.DOCKER_CONTAINER_START) {
                try {
                    String startCommandJson = RabbitMQUtils.readString(data);
                    StartCommandData startCommand = gson.fromJson(startCommandJson, StartCommandData.class);
                    final String containerId = Integer.toString(random.nextInt());

                    if (startCommand.image.equals(MIMICKING_ALGORITHM_DOCKER_IMAGE)) {
                        // Create the mimicking Algorithm
                        mimickingExecutor = new DummyComponentExecutor(
                                new DummyMimickingAlgorithm(startCommand.environmentVariables)) {
                            @Override
                            public void run() {
                                super.run();
                                // Send the message that the mimicking algorithm
                                // terminated
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
                        mimickingThread = new Thread(mimickingExecutor);
                        mimickingThread.start();
                        cmdChannel.basicPublish("", replyTo, MessageProperties.PERSISTENT_BASIC,
                                RabbitMQUtils.writeString(containerId));
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

    protected static class DummyMimickingAlgorithm extends AbstractComponent {

        private String env[];
        private int dataSize;
        private SimpleFileSender sender;

        public DummyMimickingAlgorithm(String[] env) {
            super();
            this.env = env;
        }

        @Override
        public void init() throws Exception {
            super.init();
            String queueName = null;
            for (String variable : env) {
                if (variable.contains(Constants.DATA_QUEUE_NAME_KEY)) {
                    queueName = variable.replace(Constants.DATA_QUEUE_NAME_KEY + "=", "");
                } else if (variable.contains(DATA_SIZE_KEY)) {
                    dataSize = Integer.parseInt(variable.replace(DATA_SIZE_KEY + "=", ""));
                }
            }
            Objects.requireNonNull(queueName);
            // create the sender
            sender = SimpleFileSender.create(outgoingDataQueuefactory, queueName);
        }

        @Override
        public void run() throws Exception {
            LOGGER.info("Mimicking algorithm started...");
            InputStream is = null;
            try {
                // create input stream, e.g., by opening a file
                final PipedOutputStream out = new PipedOutputStream();
                is = new PipedInputStream(out);
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            byte[] linebreak = RabbitMQUtils.writeString(String.format("%n"));
                            for (int i = 0; i < dataSize; ++i) {
                                out.write(RabbitMQUtils.writeString(Integer.toString(i)));
                                out.write(linebreak);
                            }
                        } catch (Exception e) {
                            LOGGER.error("Error while generating data.", e);
                        } finally {
                            IOUtils.closeQuietly(out);
                        }
                    }
                });
                t.start();
                // send data
                sender.streamData(is, OUTPUT_FILE_NAME);
            } catch (Exception e) {
                LOGGER.error("Error while generating data.", e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }

        @Override
        public void close() throws IOException {
            super.close();
            // close the sender
            IOUtils.closeQuietly(sender);
        }
    }
}
