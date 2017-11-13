package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.hobbit.core.Commands;
import org.hobbit.core.services.RunnableServiceCapable;
import org.hobbit.core.utils.ByteChannelUtils;
import org.hobbit.core.utils.PublisherUtils;
import org.hobbit.interfaces.DataGenerator;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.hobbit.transfer.OutputStreamChunkedTransfer;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.Streams;

@Component
public class DataGeneratorFacetedBrowsing
    extends ComponentBase
    implements DataGenerator, RunnableServiceCapable
{
    private static final Logger logger = LoggerFactory.getLogger(DataGeneratorFacetedBrowsing.class);

    protected int batchSize = 10000;

    @Resource(name="commandChannel")
    protected Subscriber<ByteBuffer> commandChannel;

    @Resource
    protected TripleStreamSupplier tripleStreamSupplier;

    @Resource(name="dg2tg")
    protected Subscriber<ByteBuffer> toTaskGenerator;

    @Resource(name="dg2sa")
    protected Subscriber<ByteBuffer> toSystemAdatper;



    protected CompletableFuture<ByteBuffer> startSignalFuture;


    @Override
    public void startUp() {
        try { init(); } catch(Exception e) { throw new RuntimeException(e); }
    }
    @Override
    public void shutDown() {
        try { close(); } catch(Exception e) { throw new RuntimeException(e); }
    }




    @Override
    public void init() throws Exception {
        logger.debug("Data generator init");


        startSignalFuture = PublisherUtils.triggerOnMessage(commandPublisher,
                ByteChannelUtils.firstByteEquals(Commands.DATA_GENERATOR_START_SIGNAL));


        // This code block would allow repeated requests to the DG, but
        // but the protocol demands the DG to shut down after handling a single DG request
        if(false) {
            startSignalFuture.whenComplete((buffer, t) -> {
                try {
                    generateData();
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }

                // FIXME shut down the service
                try {
                    close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        
        commandChannel.onNext(ByteBuffer.wrap(new byte[] {Commands.DATA_GENERATOR_READY_SIGNAL}));
    }

    @Override
    public void run() {
        logger.debug("Waiting for message to start data generation");

        try {
            startSignalFuture.get(60, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }

        logger.debug("Data generation start message received.");

        try {
            generateData();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        logger.debug("Data generation finished, shutting myself down");
    }


//    @Override
//    public void receiveCommand(byte command, byte[] data) {
//        logger.debug("Seen command: " + command);
//        if(command == Commands.DATA_GENERATOR_START_SIGNAL) {
//            try {
//                generateData();
//            } catch(Exception e) {
//                 throw new RuntimeException(e);
//            }
//        }
//
////        if (command == (byte) 150 ) {
////            byte[] emptyByte = {};
////            try {
////                // tell the task generator to start
////                sendDataToTaskGenerator(emptyByte);
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////            generateTasks.release();
////        }
//    }


//        streamManager.handleIncomingData(ByteBuffer.wrap(data));


    // TODO Separate actual data generation / caching from sending out platform specific events
    @Override
    public void generateData() throws Exception {
        logger.info("Data generator started.");

        // Generate the data once and store it in a temp file
        // TODO Add a flag whether to cache in a file
        File datasetFile = File.createTempFile("hobbit-data-generator-faceted-browsing", ".nt");
        datasetFile.deleteOnExit();
        Stream<Triple> triples = tripleStreamSupplier.get();
        RDFDataMgr.writeTriples(new FileOutputStream(datasetFile), triples.iterator());

        Supplier<Stream<Triple>> triplesFromCache = () -> {
            try {
                return Streams.stream(RDFDataMgr.createIteratorTriples(new FileInputStream(datasetFile), Lang.NTRIPLES, "http://example.org/"));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        };

        logger.debug("Data generator is sending dataset to task generater");
        sendTriples(triplesFromCache.get(), batchSize, toTaskGenerator::onNext);
        
        logger.debug("Data generator is sending dataset to system adapter");
        sendTriples(triplesFromCache.get(), batchSize, toSystemAdatper::onNext);

        logger.debug("Data generator fulfilled its purpose and shuts down");
        datasetFile.delete();
    }

    public static Entry<Long, Long> sendTriples(Stream<Triple> stream, int batchSize, Consumer<ByteBuffer> channel) throws IOException {

        AtomicLong recordCount = new AtomicLong();
        AtomicLong batchCount = new AtomicLong();

        //ChunkedProtocolWriter protocol = new ChunkedProtocolWriterSimple(666);

        //Stream<Triple> stream = Streams.stream(it);
        try(OutputStream out = OutputStreamChunkedTransfer.newInstanceForByteChannel(channel, null)) {
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
                    //logger.info("CONVERTED MODEL TO BYTE. SENDING TO TASK");
                    //logger.info("byte array: " + data);

                    String graphURI = "http://www.example.com/graph";

                    try {
                        out.write(data);
                        //channel.write(ByteBuffer.wrap(data));
                        //sink.accept(RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(graphURI)}, data));
                        //sendDataToSystemAdapter(RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(graphURI)}, data));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            out.flush();
        }

        Entry<Long, Long> result = new SimpleEntry<>(recordCount.get(), batchCount.get());
        return result;
    }

    @Override
    public void close() throws IOException {
    }

}
