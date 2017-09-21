package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.hobbit.core.components.test.InMemoryEvaluationStore.ResultImpl;
import org.hobbit.core.components.test.InMemoryEvaluationStore.ResultPairImpl;
import org.hobbit.core.data.Result;
import org.hobbit.core.data.ResultPair;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.transfer.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class InMemoryEvaluationStorage
    extends ComponentBase
{
    private static final Logger logger = LoggerFactory.getLogger(InMemoryEvaluationStorage.class);

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



    protected Map<String, Result> taskIdToExpectedResult = new LinkedHashMap<>();
    protected Map<String, Result> taskIdToActualResult = new LinkedHashMap<>();



    Stream<Entry<String, Entry<Result, Result>>> streamResults() {
        return streamPairs(taskIdToExpectedResult, taskIdToActualResult);
    }

    /**
     * Creates a stream of entries of the form (keyCommonToBothMaps, (valueForKeyInA, valueForkeyInB))
     *
     * @param a
     * @param b
     * @return
     */
    public static Stream<Entry<String, Entry<Result, Result>>>
        streamPairs(Map<String, Result> a, Map<String, Result> b)
    {
        Set<String> keys = Sets.union(a.keySet(), b.keySet());

        Stream<Entry<String, Entry<Result, Result>>> result = keys.stream()
                .map(key -> new SimpleEntry<>(key, new SimpleEntry<>(
                        a.get(key),
                        b.get(key))));

        return result;
    }

    @Resource(name="tg2es")
    protected Publisher<ByteBuffer> expectedResultsFromTaskGenerator;

    @Resource(name="sa2es")
    protected Publisher<ByteBuffer> actualResultsFromTaskGenerator;

    @Resource(name="em2esPub")
    protected Publisher<ByteBuffer> fromEvaluationModule;

    @Resource(name="es2em")
    protected WritableByteChannel toEvaluationModule;


    public Iterator<ResultPair> createIterator() {
        return streamResults()
            .map(
                e -> {
                    ResultPairImpl r = new ResultPairImpl();
                    r.setActual(e.getValue().getKey());
                    r.setExpected(e.getValue().getValue());
                    return (ResultPair)r;
                })
            .iterator();
    }

    public void init() {
        // TODO We could add detection of duplicate keys

        expectedResultsFromTaskGenerator.subscribe(data -> {
            parseMessageIntoResultAndPassToConsumer(data, taskIdToExpectedResult::put);
        });

        actualResultsFromTaskGenerator.subscribe(data -> {
            parseMessageIntoResultAndPassToConsumer(data, taskIdToActualResult::put);
        });


        fromEvaluationModule.subscribe(buffer -> {
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
    public void close() throws IOException {
        // TODO We should unsubscribe the consumers
    }

}
