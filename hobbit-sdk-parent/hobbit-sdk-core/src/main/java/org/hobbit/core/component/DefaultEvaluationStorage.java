package org.hobbit.core.component;

import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.hobbit.core.components.test.InMemoryEvaluationStore.ResultImpl;
import org.hobbit.core.components.test.InMemoryEvaluationStore.ResultPairImpl;
import org.hobbit.core.data.Result;
import org.hobbit.core.data.ResultPair;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.storage.Storage;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import io.reactivex.Flowable;


/**
 * The evaluation storage implementing the default protocol for
 * communication with
 * - sa (receiving of actual results)
 * - tg (receiving of expected results)
 * - em (some complex rpc pattern)
 *
 * This component delegates all requests to a Storage class
 *
 *
 * @author raven Sep 21, 2017
 *
 */
@Component
@Qualifier("MainService")
public class DefaultEvaluationStorage
    extends ComponentBaseIdleService
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultEvaluationStorage.class);


    @Autowired
    protected Storage<String, Result> storage;


    /**
     * If a request contains this iterator ID, a new iterator is created and its
     * first result as well as its Id are returned.
     */
    public static final byte NEW_ITERATOR_ID = -1;
    /**
     * The empty response that is sent if an error occurs.
     */
    private static final byte[] EMPTY_RESPONSE = new byte[0];

    /**
     * Iterators that have been started.
     */
    protected List<Iterator<ResultPair>> resultPairIterators = Lists.newArrayList();


    @Resource(name="tg2esReceiver")
    protected Flowable<ByteBuffer> expectedResultsFromTaskGenerator;

    @Resource(name="sa2esReceiver")
    protected Flowable<ByteBuffer> actualResultsFromSystemAdapter;

    @Resource(name="em2esReceiver")
    protected Flowable<ByteBuffer> fromEvaluationModule;

    @Resource(name="es2emSender")
    protected Subscriber<ByteBuffer> toEvaluationModule;

    @Resource(name="taskAckSender")
    protected Subscriber<ByteBuffer> taskAck;


    public Iterator<ResultPair> createIterator() {
        return storage.streamResults()
            .map(
                e -> {
                    ResultPairImpl r = new ResultPairImpl();
                    Entry<Result, Result> expectedAndActual = e.getValue();
                    r.setExpected(expectedAndActual.getKey());
                    r.setActual(expectedAndActual.getValue());
                    return (ResultPair)r;
                })
            .iterator();
    }

    @Override
    public void startUp() {
    	super.startUp();
    	boolean[] resultSent = {false};
    	
        // TODO We could add detection of duplicate keys

        expectedResultsFromTaskGenerator.subscribe(data -> {
            logger.debug("Got expected result from task generator");

            if(resultSent[0]) {
            	throw new RuntimeException("Evaluation module already requested data, yet an attempt to add another expected result was seen");
            }
            
            Entry<String, Result> record = parseMessage(data);
            storage.putExpectedValue(record.getKey(), record.getValue());
        });

        actualResultsFromSystemAdapter.subscribe(data -> {
            logger.debug("Got actual result from system adapter");

            if(resultSent[0]) {
            	throw new RuntimeException("Evaluation module already requested data, yet an attempt to add another actual result was seen");
            }
            
            //final String ackExchangeName = generateSessionQueueName(Constants.HOBBIT_ACK_EXCHANGE_NAME);
            Entry<String, Result> record = parseMessage(data);
            String taskId = record.getKey();

            logger.info("Acknowledging received evaluation data for task " + taskId);
            taskAck.onNext(ByteBuffer.wrap(RabbitMQUtils.writeString(taskId)));
            
            storage.putActualValue(taskId, record.getValue());
        });


        fromEvaluationModule.subscribe(buffer -> {
        	buffer = buffer.duplicate();
            logger.info("Got request from evaluation module: " + Arrays.toString(buffer.array())); // storage contains " + Iterators.size(createIterator()) + " items");

            //while(true) {
                byte response[] = null;
                // get iterator id
                if (buffer.remaining() < 1) {
                    response = EMPTY_RESPONSE;
                    logger.error("Got a request without a valid iterator Id. Returning emtpy response.");
                } else {
                	
                	resultSent[0] = true;

                    byte iteratorId = buffer.get();

                    // get the iterator
                    Iterator<ResultPair> iterator = null;
                    if (iteratorId == NEW_ITERATOR_ID) {
                        // create and save a new iterator
                        iteratorId = (byte) resultPairIterators.size();
                        logger.info("Creating new iterator #{}", iteratorId);
                        resultPairIterators.add(iterator = createIterator());
                    } else if ((iteratorId < 0) || iteratorId >= resultPairIterators.size()) {
                        response = EMPTY_RESPONSE;
                        logger.error("Got a request without a valid iterator Id (" + Byte.toString(iteratorId)
                                + "). Returning emtpy response.");
                    } else {
                        iterator = resultPairIterators.get(iteratorId);
                    }
                    if ((iterator != null) && (iterator.hasNext())) {
                        ResultPair resultPair = iterator.next();
                        // set response (iteratorId,
                        // taskSentTimestamp, expectedData,
                        // responseReceivedTimestamp, receivedData)
                        Result expected = resultPair.getExpected();
                        Result actual = resultPair.getActual();

                        response = RabbitMQUtils
                                .writeByteArrays(
                                        new byte[] {
                                                iteratorId },
                                        new byte[][] {
                                                expected != null
                                                        ? RabbitMQUtils.writeLong(expected.getSentTimestamp())
                                                        : new byte[0],
                                                expected != null ? expected.getData() : new byte[0],
                                                actual != null
                                                        ? RabbitMQUtils.writeLong(actual.getSentTimestamp())
                                                        : new byte[0],
                                                actual != null ? actual.getData() : new byte[0] },
                                        null);
                    } else {
                        response = new byte[] { iteratorId };
                    }
                }
                //getChannel().basicPublish("", properties.getReplyTo(), null, response);

                // FIXME This might be the first time I see RabbitMQ feature used that can not be covered
                // by the ByteChannel abstraction: Replying on the channel that sent the request

                // Fortunately, at present, we can just use the static channel to the em
//                try {
                    logger.info("Sending " + response.length + " bytes to evaluation module");
                    toEvaluationModule.onNext(ByteBuffer.wrap(response));
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
            //}
        });


    }

    
    public static Entry<String, Result> parseMessage(ByteBuffer buffer) {
        String taskId = RabbitMQUtils.readString(buffer);
        byte[] taskData = RabbitMQUtils.readByteArray(buffer);

        //System.out.println("For " + consumer + " Received taskId " + taskId);
        
        // FIMXE hack for timestamps
        long timestamp = buffer.hasRemaining() ? buffer.getLong() : System.currentTimeMillis();

        Result r = new ResultImpl(timestamp, taskData);

        Entry<String, Result> result = new SimpleEntry<>(taskId, r);
        return result;
    }


//    // TODO Move to some util function
//    public static void parseMessageIntoResultAndPassToConsumer(ByteBuffer buffer, BiConsumer<String, Result> consumer) {
//        String taskId = RabbitMQUtils.readString(buffer);
//        byte[] taskData = RabbitMQUtils.readByteArray(buffer);
//
//        //System.out.println("For " + consumer + " Received taskId " + taskId);
//        
//        // FIMXE hack for timestamps
//        long timestamp = buffer.hasRemaining() ? buffer.getLong() : System.currentTimeMillis();
//
//        Result result = new ResultImpl(timestamp, taskData);
//        consumer.accept(taskId, result);
//    }

    @Override
    public void shutDown() {
    	super.shutDown();
        logger.debug("evaluation storage shut down done");
    }
}
