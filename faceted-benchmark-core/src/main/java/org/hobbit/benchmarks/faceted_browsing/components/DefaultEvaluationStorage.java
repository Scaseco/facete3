package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;

import javax.annotation.Resource;

import org.hobbit.core.components.test.InMemoryEvaluationStore.ResultImpl;
import org.hobbit.core.components.test.InMemoryEvaluationStore.ResultPairImpl;
import org.hobbit.core.data.Result;
import org.hobbit.core.data.ResultPair;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.services.IdleServiceCapable;
import org.hobbit.transfer.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;


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
public class DefaultEvaluationStorage
    extends ComponentBase
    implements IdleServiceCapable
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


    @Resource(name="tg2es")
    protected Publisher<ByteBuffer> expectedResultsFromTaskGenerator;

    @Resource(name="sa2es")
    protected Publisher<ByteBuffer> actualResultsFromTaskGenerator;

    @Resource(name="em2esPub")
    protected Publisher<ByteBuffer> fromEvaluationModule;

    @Resource(name="es2em")
    protected WritableByteChannel toEvaluationModule;


    public Iterator<ResultPair> createIterator() {
        return storage.streamResults()
            .map(
                e -> {
                    ResultPairImpl r = new ResultPairImpl();
                    r.setActual(e.getValue().getKey());
                    r.setExpected(e.getValue().getValue());
                    return (ResultPair)r;
                })
            .iterator();
    }

    @Override
    public void startUp() {
        // TODO We could add detection of duplicate keys

        expectedResultsFromTaskGenerator.subscribe(data -> {
            parseMessageIntoResultAndPassToConsumer(data, storage::putExpectedValue);
        });

        actualResultsFromTaskGenerator.subscribe(data -> {
            parseMessageIntoResultAndPassToConsumer(data, storage::putActualValue);
        });


        fromEvaluationModule.subscribe(buffer -> {
            logger.debug("Got data for evaluation from storage");

            //while(true) {
                byte response[] = null;
                // get iterator id
                if (buffer.remaining() < 1) {
                    response = EMPTY_RESPONSE;
                    logger.error("Got a request without a valid iterator Id. Returning emtpy response.");
                } else {
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
                try {
                    toEvaluationModule.write(ByteBuffer.wrap(response));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            //}
        });


    }


    // TODO Move to some util function
    public static void parseMessageIntoResultAndPassToConsumer(ByteBuffer buffer, BiConsumer<String, Result> consumer) {
        String taskId = RabbitMQUtils.readString(buffer);
        byte[] taskData = RabbitMQUtils.readByteArray(buffer);

        // FIMXE hack for timestamps
        long timestamp = buffer.hasRemaining() ? buffer.getLong() : System.currentTimeMillis();

        Result result = new ResultImpl(timestamp, taskData);
System.out.println("Got message from system adapter");
        consumer.accept(taskId, result);
    }

    @Override
    public void shutDown() throws Exception {
        // TODO Auto-generated method stub

    }
}
