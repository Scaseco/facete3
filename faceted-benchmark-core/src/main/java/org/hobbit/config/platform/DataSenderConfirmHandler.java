package org.hobbit.config.platform;

import java.io.IOException;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;

public class DataSenderConfirmHandler implements ConfirmListener {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DataSenderConfirmHandler.class);

	protected Channel channel;
	protected String queueName;
	
    public static class Message {
        public BasicProperties properties;
        public byte[] data;

        public Message(BasicProperties properties, byte[] data) {
            this.properties = properties;
            this.data = data;
        }
    }

	
    private final Semaphore maxBufferedMessageCount;
    private final SortedMap<Long, Message> unconfirmedMsgs = Collections
            .synchronizedSortedMap(new TreeMap<Long, Message>());
    private int successfullySubmitted = 0;

    public DataSenderConfirmHandler(int messageConfirmBuffer) {
        this.maxBufferedMessageCount = new Semaphore(messageConfirmBuffer);
    }

    public synchronized void sendDataWithConfirmation(BasicProperties properties, byte[] data) throws IOException {
        try {
            LOGGER.trace("{}\tavailable\t{}", toString(),
                    maxBufferedMessageCount.availablePermits());
            maxBufferedMessageCount.acquire();
        } catch (InterruptedException e) {
            throw new IOException("Interrupted while waiting for free buffer to store the message before sending.",
                    e);
        }
        synchronized (unconfirmedMsgs) {
            sendData_unsecured(new Message(properties, data));
        }
    }

    private void sendData_unsecured(Message message) throws IOException {
        // Get ownership of the channel to make sure that nobody else is
        // using it while we get the next sequence number and send the next
        // data
        synchronized (channel) {
            long sequenceNumber = channel.getNextPublishSeqNo();
            LOGGER.trace("{}\tsending\t{}", toString(), sequenceNumber);
            unconfirmedMsgs.put(sequenceNumber, message);
//            try {
//                sendData(message.properties, message.data);
//            } catch (IOException e) {
//                // the message hasn't been sent, remove it from the set
//                unconfirmedMsgs.remove(sequenceNumber);
//                maxBufferedMessageCount.release();
//                throw e;
//            }
        }
    }

    @Override
    public void handleAck(long deliveryTag, boolean multiple) throws IOException {
        synchronized (unconfirmedMsgs) {
            if (multiple) {
                // Remove all acknowledged messages
                SortedMap<Long, Message> negativeMsgs = unconfirmedMsgs.headMap(deliveryTag + 1);
                int ackMsgCount = negativeMsgs.size();
                negativeMsgs.clear();
                maxBufferedMessageCount.release(ackMsgCount);
                successfullySubmitted += ackMsgCount;
                LOGGER.trace("{}\tack\t{}+\t{}", toString(), deliveryTag,
                        maxBufferedMessageCount.availablePermits());
            } else {
                // Remove the message
                unconfirmedMsgs.remove(deliveryTag);
                ++successfullySubmitted;
                maxBufferedMessageCount.release();
                LOGGER.trace("{}\tack\t{}\t{}", toString(), deliveryTag,
                        maxBufferedMessageCount.availablePermits());
            }
        }
    }

    @Override
    public void handleNack(long deliveryTag, boolean multiple) throws IOException {
        synchronized (unconfirmedMsgs) {
            LOGGER.trace("nack\t{}{}", deliveryTag, (multiple ? "+" : ""));
            if (multiple) {
                // Resend all lost messages
                SortedMap<Long, Message> negativeMsgs = unconfirmedMsgs.headMap(deliveryTag + 1);
                Message messageToResend[] = negativeMsgs.values().toArray(new Message[negativeMsgs.size()]);
                negativeMsgs.clear();
                for (int i = 0; i < messageToResend.length; ++i) {
                    sendData_unsecured(messageToResend[i]);
                }
            } else {
                if (unconfirmedMsgs.containsKey(deliveryTag)) {
                    // send the lost message again
                    Message message = unconfirmedMsgs.remove(deliveryTag);
                    sendData_unsecured(message);
                } else {
                    LOGGER.warn(
                            "Got a negative acknowledgement (nack) for an unknown message. It will be ignored.");
                }
            }
        }
    }

    public void waitForConfirms() throws InterruptedException {
        while (true) {
            synchronized (unconfirmedMsgs) {
                if (unconfirmedMsgs.size() == 0) {
                    LOGGER.trace("sent {} messages.", successfullySubmitted);
                    return;
                }
            }
            Thread.sleep(200);
        }
    }

}