package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.hobbit.core.components.test.InMemoryEvaluationStore.ResultImpl;
import org.hobbit.core.data.Result;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.transfer.Publisher;


public class InMemoryEvaluationStorage
    extends ComponentBase
{
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

    public void init() {
        // TODO We could add detection of duplicate keys

        expectedResultsFromTaskGenerator.subscribe(data -> {
            parseMessageIntoResultAndPassToConsumer(data, taskIdToExpectedResult::put);
        });

        actualResultsFromTaskGenerator.subscribe(data -> {
            parseMessageIntoResultAndPassToConsumer(data, taskIdToActualResult::put);
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
