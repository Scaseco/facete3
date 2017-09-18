package org.hobbit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.transfer.ChunkedProtocolWriter;
import org.hobbit.transfer.ChunkedProtocolWriterSimple;
import org.hobbit.transfer.OutputStreamChunkedTransfer;
import org.hobbit.transfer.StreamManager;
import org.hobbit.transfer.InputStreamManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Revised by Claus Stadler 14/09/2017.
 * Created by gkatsimpras on 28/2/2017.
 */
public class DataGeneratorFacetedBrowsingBenchmark extends AbstractDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataGeneratorFacetedBrowsingBenchmark.class);

    // FIXME How to best inject the podigg data generator?
    protected Supplier<Stream<Triple>> tripleStreamSupplier;
    protected int batchSize = 10000;

    protected StreamManager streamManagerToSystemAdapter = null;//createStreamManager(


    @Override
    public void init() throws Exception {
        super.init();
        LOGGER.info("Data generator initialized");
    }

    public static StreamManager createStreamManager(CheckedConsumer<byte[]> transport) {
        Function<Integer, ChunkedProtocolWriter> protocolSupplier = ChunkedProtocolWriterSimple::new;

//
//
//        ChunkedProtocolWriter protocol = new ChunkedProtocolWriterSimple(streamId);
//        OutputStream result = new OutputStreamChunkedTransfer(
//                protocol,
//                bytes -> CheckedConsumer.wrap(transport).accept(bytes.array()),
//                () -> {}
//                );
        return null;
    }

    @Override
    protected void generateData() throws Exception {
        LOGGER.info("Starting data generation.");

        // Send the whole dataset to the task generator
        //sendTriples(stream, batchSize, (CheckedConsumer<byte[]>)this::sendDataToTaskGenerator);

        Stream<Triple> stream = tripleStreamSupplier.get();
//        try(OutputStream out = streamManagerToSystemAdapter.newOutputStream()) {
//
//            //stream.forEach(action);
//        }


        // And send the whole dataset again to the system adapter
        stream = tripleStreamSupplier.get();
        Entry<Long, Long> recordAndChunkCounts =
                sendTriples(stream, batchSize, (CheckedConsumer<byte[]>)this::sendDataToSystemAdapter);

        long batchCount = recordAndChunkCounts.getKey();

        // And send the mocha message, that loading has completed
        sendToCmdQueue((byte)151, RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeLong(batchCount)}, RabbitMQUtils.writeLong(1)));
    }


    public static Entry<Long, Long> sendTriples(Stream<Triple> stream, int batchSize, CheckedConsumer<byte[]> sink) {

        AtomicLong recordCount = new AtomicLong();
        AtomicLong batchCount = new AtomicLong();

        //Stream<Triple> stream = Streams.stream(it);
        StreamUtils
            .mapToBatch(stream, batchSize)
            .peek(x -> batchCount.incrementAndGet())
            .map(GraphUtils::toMemGraph)
            .peek(graph -> recordCount.addAndGet(graph.size()))
            .map(ModelFactory::createModelForGraph)
            .map(batchModel -> {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                RDFDataMgr.write(baos, batchModel, RDFFormat.TURTLE);
                byte[] r = baos.toByteArray();
                return r;
            })
            .forEach(data -> {
                LOGGER.info("CONVERTED MODEL TO BYTE. SENDING TO TASKG");
                LOGGER.info("byte array: " + data);

                String graphURI = "http://www.example.com/graph";

                try {
                    sink.accept(RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(graphURI)}, data));
                    //sendDataToSystemAdapter(RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(graphURI)}, data));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        Entry<Long, Long> result = new SimpleEntry<>(recordCount.get(), batchCount.get());
        return result;
    }


    @Override
    public void receiveCommand(byte command, byte[] data) {

        streamManagerToSystemAdapter.handleIncomingData(ByteBuffer.wrap(data));

        if (command == (byte) 150 ) {
            byte[] emptyByte = {};
            try {
                sendDataToTaskGenerator(emptyByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.receiveCommand(command, data);
    }

}

