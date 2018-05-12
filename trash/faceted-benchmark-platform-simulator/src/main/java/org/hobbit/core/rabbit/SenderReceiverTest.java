package org.hobbit.core.rabbit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.hobbit.core.TestConstants;
import org.hobbit.core.data.RabbitQueue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

@RunWith(Parameterized.class)
public class SenderReceiverTest {

    private static final String QUEUE_NAME = "sender-receiver-test";

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();
        testConfigs.add(new Object[] { 1, 1, 10000, 1, 0 });
        testConfigs.add(new Object[] { 1, 1, 10000, 100, 0 });
        testConfigs.add(new Object[] { 2, 1, 10000, 1, 0 });
        testConfigs.add(new Object[] { 2, 1, 10000, 100, 0 });
        testConfigs.add(new Object[] { 1, 2, 10000, 1, 0 });
        testConfigs.add(new Object[] { 1, 2, 10000, 100, 0 });
        testConfigs.add(new Object[] { 1, 1, 1000, 100, 1000 });
        testConfigs.add(new Object[] { 2, 1, 1000, 100, 1000 });
        testConfigs.add(new Object[] { 1, 2, 1000, 100, 1000 });
        testConfigs.add(new Object[] { 1, 1, 50, 1, 500 });
        testConfigs.add(new Object[] { 2, 1, 50, 1, 500 });
        testConfigs.add(new Object[] { 1, 2, 50, 1, 500 });
        return testConfigs;
    }

    private int numberOfSenders;
    private int numberOfReceivers;
    private int numberOfMessages;
    private int numberOfMessagesProcessedInParallel;
    private long messageProcessingDelay;

    public SenderReceiverTest(int numberOfSenders, int numberOfReceivers, int numberOfMessages,
            int numberOfMessagesProcessedInParallel, long messageProcessingDelay) {
        this.numberOfSenders = numberOfSenders;
        this.numberOfReceivers = numberOfReceivers;
        this.numberOfMessages = numberOfMessages;
        this.numberOfMessagesProcessedInParallel = numberOfMessagesProcessedInParallel;
        this.messageProcessingDelay = messageProcessingDelay;
    }

    @Test
    public void test() throws Exception {
        int overallMessages = numberOfSenders * numberOfMessages;
        RabbitQueueFactoryImpl sendQueueFactory = null;
        RabbitQueueFactoryImpl receiveQueueFactory = null;

        try {
            ConnectionFactory cFactory = new ConnectionFactory();
            cFactory.setHost(TestConstants.RABBIT_HOST);
            cFactory.setAutomaticRecoveryEnabled(true);
            sendQueueFactory = new RabbitQueueFactoryImpl(cFactory.newConnection());

            Receiver receivers[] = new Receiver[numberOfReceivers];
            Thread receiverThreads[] = new Thread[numberOfReceivers];
            for (int i = 0; i < receiverThreads.length; ++i) {
                receivers[i] = new Receiver(cFactory, overallMessages, numberOfMessagesProcessedInParallel,
                        messageProcessingDelay);
                receiverThreads[i] = new Thread(receivers[i]);
                receiverThreads[i].start();
            }

            Sender senders[] = new Sender[numberOfSenders];
            Thread senderThreads[] = new Thread[numberOfSenders];
            for (int i = 0; i < senderThreads.length; ++i) {
                senders[i] = new Sender(i, numberOfMessages, sendQueueFactory);
                senderThreads[i] = new Thread(senders[i]);
                senderThreads[i].start();
            }
            for (int i = 0; i < senderThreads.length; ++i) {
                senderThreads[i].join();
            }
            for (int i = 0; i < receivers.length; ++i) {
                receivers[i].terminate();
            }
            for (int i = 0; i < receiverThreads.length; ++i) {
                receiverThreads[i].join();
            }
            // Make sure no errors occurred
            for (int i = 0; i < senders.length; ++i) {
                Assert.assertNull(senders[i].getError());
            }
            for (int i = 0; i < receivers.length; ++i) {
                Assert.assertNull(receivers[i].getError());
            }
            // check received messages
            BitSet receivedMsgIds = new BitSet(overallMessages);
            for (int i = 0; i < receivers.length; ++i) {
                Assert.assertFalse(receivedMsgIds.intersects(receivers[i].getReceivedMsgIds()));
                receivedMsgIds.or(receivers[i].getReceivedMsgIds());
            }
            Assert.assertEquals(overallMessages, receivedMsgIds.cardinality());
        } finally {
            IOUtils.closeQuietly(sendQueueFactory);
            IOUtils.closeQuietly(receiveQueueFactory);
        }
    }

    protected static class Sender implements Runnable {

        private static final Logger LOGGER = LoggerFactory.getLogger(Sender.class);

        private int senderId;
        private int numberOfMessages;
        private RabbitQueueFactory factory;
        private Throwable error;

        public Sender(int senderId, int numberOfMessages, RabbitQueueFactory factory) {
            this.senderId = senderId;
            this.numberOfMessages = numberOfMessages;
            this.factory = factory;
        }

        @Override
        public void run() {
            DataSender sender = null;
            try {
                sender = DataSenderImpl.builder().queue(factory, QUEUE_NAME).build();
                int firstMsgId = senderId * numberOfMessages;
                for (int i = 0; i < numberOfMessages; ++i) {
                    sender.sendData(RabbitMQUtils.writeString(Integer.toString(firstMsgId + i)));
                }
                sender.closeWhenFinished();
            } catch (Exception e) {
                LOGGER.error("Sender crashed with Exception.", e);
                error = e;
            } finally {
                IOUtils.closeQuietly(sender);
            }
        }

        public Throwable getError() {
            return error;
        }

    }

    protected static class Receiver implements Runnable, DataHandler {

        private static final Logger LOGGER = LoggerFactory.getLogger(Receiver.class);

        private ConnectionFactory cFactory;
        private BitSet receivedMsgIds;
        private int maxParallelProcessedMsgs;
        private long messageProcessingDelay;
        private Semaphore terminationMutex = new Semaphore(0);
        private Throwable error;

        public Receiver(ConnectionFactory cFactory, int overallMessages, int maxParallelProcessedMsgs,
                long messageProcessingDelay) {
            super();
            receivedMsgIds = new BitSet(overallMessages);
            this.cFactory = cFactory;
            this.maxParallelProcessedMsgs = maxParallelProcessedMsgs;
            this.messageProcessingDelay = messageProcessingDelay;
        }

        @Override
        public void run() {
            RabbitQueueFactory factory = null;
            RabbitQueue queue = null;
            // ExecutorService executor = null;
            try {
                // executor =
                // Executors.newFixedThreadPool(maxParallelProcessedMsgs);
                // factory = new
                // RabbitQueueFactoryImpl(cFactory.newConnection());
                // queue = factory.createDefaultRabbitQueue(QUEUE_NAME);
                // queue.channel.basicQos(0, maxParallelProcessedMsgs, false);
                // if (maxParallelProcessedMsgs == 1) {
                // receiveMsgsSequentielly(queue);
                // } else if (maxParallelProcessedMsgs > 1) {
                // receiveMsgsInParallel(queue, executor);
                // }
                factory = new RabbitQueueFactoryImpl(cFactory.newConnection());
                DataReceiver receiver = DataReceiverImpl.builder().dataHandler(this)
                        .maxParallelProcessedMsgs(maxParallelProcessedMsgs).queue(factory, QUEUE_NAME).build();
                terminationMutex.acquire();
                receiver.closeWhenFinished();
            } catch (Exception e) {
                LOGGER.error("Receiver crashed with Exception.", e);
                error = e;
            } finally {
                IOUtils.closeQuietly(queue);
                IOUtils.closeQuietly(factory);
            }
        }

        @SuppressWarnings("unused")
        private void receiveMsgsSequentielly(RabbitQueue queue) throws Exception {
            QueueingConsumer consumer = new QueueingConsumer(queue.channel);
            queue.channel.basicConsume(queue.name, true, consumer);
            Delivery delivery = null;
            while ((terminationMutex.availablePermits() == 0) || (queue.messageCount() > 0) || (delivery != null)) {
                delivery = consumer.nextDelivery(3000);
                if (delivery != null) {
                    processMsg(RabbitMQUtils.readString(delivery.getBody()));
                }
            }
        }

        @SuppressWarnings("unused")
        private void receiveMsgsInParallel(RabbitQueue queue, ExecutorService executor) throws Exception {
            QueueingConsumer consumer = new QueueingConsumer(queue.channel);
            queue.channel.basicConsume(queue.name, true, consumer);
            Delivery delivery = null;
            while ((terminationMutex.availablePermits() == 0) || (queue.messageCount() > 0) || (delivery != null)) {
                delivery = consumer.nextDelivery(3000);
                if (delivery != null) {
                    executor.submit(new MsgProcessingTask(delivery));
                }
            }
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.DAYS);
        }

        protected class MsgProcessingTask implements Runnable {

            private Delivery delivery;

            public MsgProcessingTask(Delivery delivery) {
                this.delivery = delivery;
            }

            @Override
            public void run() {
                processMsg(RabbitMQUtils.readString(delivery.getBody()));
            }

        }

        @SuppressWarnings("unused")
        private void receiveMsgsInParallel_old(RabbitQueue queue, ExecutorService executor) throws Exception {
            final Semaphore currentlyProcessedMessages = new Semaphore(maxParallelProcessedMsgs);
            queue.channel.basicConsume(queue.name, true, new DefaultConsumer(queue.channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties,
                        byte[] body) throws IOException {
                    try {
                        currentlyProcessedMessages.acquire();
                        try {
                            processMsg(RabbitMQUtils.readString(body));
                        } catch (Exception e) {
                            LOGGER.error("Got exception while trying to process incoming data.", e);
                        } finally {
                            currentlyProcessedMessages.release();
                        }
                    } catch (InterruptedException e) {
                        throw new IOException("Interrupted while waiting for mutex.", e);
                    }
                }
            });
            terminationMutex.acquire();
            // wait until all messages have been read from the queue
            long messageCount = queue.messageCount();
            int count = 0;
            while (count < 5) {
                if (messageCount > 0) {
                    LOGGER.info("Waiting for remaining data to be processed: " + messageCount);
                    count = 0;
                } else {
                    ++count;
                }
                Thread.sleep(200);
                messageCount = queue.messageCount();
            }
            executor.shutdown();
            executor.awaitTermination(120, TimeUnit.SECONDS);
            LOGGER.info("Waiting data processing to finish... ( {} / {} free permits are available)",
                    currentlyProcessedMessages.availablePermits(), maxParallelProcessedMsgs);
            currentlyProcessedMessages.acquire(maxParallelProcessedMsgs);
        }

        private void processMsg(String msg) {
            int id = Integer.parseInt(msg);
            synchronized (receivedMsgIds) {
                if (receivedMsgIds.get(id)) {
                    LOGGER.error("Received id {} a second time.", id);
                    if (error != null) {
                        error = new IllegalStateException("Received at least one message twice");
                    }
                } else {
                    receivedMsgIds.set(id);
                }
            }
            try {
                Thread.sleep(messageProcessingDelay);
            } catch (InterruptedException e) {
            }
        }

        public BitSet getReceivedMsgIds() {
            return receivedMsgIds;
        }

        public void terminate() {
            terminationMutex.release();
        }

        public Throwable getError() {
            return error;
        }

        @Override
        public void handleData(byte[] data) {
            processMsg(RabbitMQUtils.readString(data));
        }
    }
}
