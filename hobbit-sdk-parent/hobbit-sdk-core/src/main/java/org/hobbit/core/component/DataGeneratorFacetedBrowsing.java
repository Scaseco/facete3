package org.hobbit.core.component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
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

import org.aksw.commons.collections.cache.CountingIterator;
import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.apache.commons.io.Charsets;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.hobbit.core.Commands;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.service.api.RunnableServiceCapable;
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
import com.google.common.primitives.Bytes;

@Component
public class DataGeneratorFacetedBrowsing
    extends ComponentBase
    implements DataGenerator, RunnableServiceCapable
{
    private static final Logger logger = LoggerFactory.getLogger(DataGeneratorFacetedBrowsing.class);

    protected int batchSize = 10000;

    @Resource(name="commandSender")
    protected Subscriber<ByteBuffer> commandSender;

    @Resource
    protected TripleStreamSupplier tripleStreamSupplier;

    @Resource(name="dg2tgSender")
    protected Subscriber<ByteBuffer> toTaskGenerator;

    @Resource(name="dg2saSender")
    protected Subscriber<ByteBuffer> toSystemAdatper;



    protected CompletableFuture<ByteBuffer> startSignalFuture;


//    @Override
//    public void startUp() {
//        try { init(); } catch(Exception e) { throw new RuntimeException(e); }
//    }
//
//    
//    @Override
//    public void shutDown() {
//        try { close(); } catch(Exception e) { throw new RuntimeException(e); }
//    }




    @Override
    public void startUp() throws Exception {
        logger.debug("DataGenerator::startUp()");
    	super.startUp();


        startSignalFuture = PublisherUtils.triggerOnMessage(commandReceiver,
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
        
        commandSender.onNext(ByteBuffer.wrap(new byte[] {Commands.DATA_GENERATOR_READY_SIGNAL}));
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
        File datasetFile = File.createTempFile("hobbit-data-generator-faceted-browsing-", ".nt");
        
        // TODO Enable an optional validation step which keeps the output files if there are any problems
        
        datasetFile.deleteOnExit();
        try(Stream<Triple> triples = tripleStreamSupplier.get()) {
        	CountingIterator<Triple> it = new CountingIterator<>(triples.iterator());
        	
        	RDFDataMgr.writeTriples(new FileOutputStream(datasetFile), it);
        	System.out.println("count: " + it.getNumItems());
        }
        
        Supplier<Stream<Triple>> triplesFromCache = () -> {
            try {
                return Streams.stream(RDFDataMgr.createIteratorTriples(new FileInputStream(datasetFile), Lang.NTRIPLES, "http://example.org/"));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        };

        
        //commandSender.onNext((ByteBuffer)ByteBuffer.allocate(1).put(MochaConstants.BULK_LOAD_FROM_DATAGENERATOR).rewind());
        
        logger.info("Data generator is sending dataset to task generator");
        sendTriplesViaMochaProtocol(triplesFromCache.get(), batchSize, toTaskGenerator::onNext);
        //sendTriplesViaStreamProtocol(triplesFromCache.get(), batchSize, toTaskGenerator::onNext);
        
        logger.info("Data generator is sending dataset to system adapter");
        Entry<Long, Long> numRecordsAndBatches = sendTriplesViaMochaProtocol(triplesFromCache.get(), batchSize, toSystemAdatper::onNext);

    	// Notify the BC about this DC's contribution to the dataset generation
    	commandSender.onNext((ByteBuffer)ByteBuffer.allocate(17)
    			.put(MochaConstants.BULK_LOAD_FROM_DATAGENERATOR)
    			.putLong(numRecordsAndBatches.getKey())
    			.putLong(numRecordsAndBatches.getValue())
    			.rewind());

        datasetFile.delete();
        logger.info("Data generator fulfilled its purpose and shuts down");
    }

    /**
     * 
     * 
     * @param stream
     * @param batchSize
     * @param channel
     * @throws IOException
     */
    public static Entry<Long, Long> sendTriplesViaMochaProtocol(Stream<Triple> stream, int batchSize, Consumer<ByteBuffer> channel) throws IOException {
    	Entry<Long, Long> numRecordsAndBatches = sendTriplesRaw(stream, batchSize, channel, true);
    	return numRecordsAndBatches;
    }
    
    public static Consumer<ByteBuffer> wrapAsConsumer(OutputStream out) {
    	return buf -> {
    		try {
    			out.write(buf.array());
    		} catch(Exception e) {
    			throw new RuntimeException(e);
    		}
    	};

    }
    
    public static Entry<Long, Long> sendTriplesViaStreamProtocol(Stream<Triple> stream, int batchSize, Consumer<ByteBuffer> channel) throws IOException {
    	Entry<Long, Long> result;
    	try(OutputStream out = OutputStreamChunkedTransfer.newInstanceForByteChannel(channel, null)) {
            Consumer<ByteBuffer> sink = wrapAsConsumer(out);
    		result = sendTriplesRaw(stream, batchSize, sink, false);
        	out.flush();
        }
        
        return result;
    }
    
    public static Entry<Long, Long> sendTriplesRaw(Stream<Triple> stream, int batchSize, Consumer<ByteBuffer> channel, boolean useMocha) throws IOException {

        AtomicLong recordCount = new AtomicLong();
        AtomicLong batchCount = new AtomicLong();

        //ChunkedProtocolWriter protocol = new ChunkedProtocolWriterSimple(666);

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

            	if(useMocha) {
	                String graphURI = "http://www.example.com/graph";
	                ///byte[] bytes = RabbitMQUtils.writeString(graphURI);
	                
	                ByteBuffer msg = ByteBuffer.wrap(Bytes.concat(
	                		ByteBuffer.allocate(4).putInt(graphURI.length()).array(),
	                		graphURI.getBytes(StandardCharsets.UTF_8),
	                		data));
	                
	            	channel.accept(msg);
            	} else {
            		channel.accept(ByteBuffer.wrap(data));
            	}
                    //channel.write(ByteBuffer.wrap(data));
                    //sink.accept(RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(graphURI)}, data));
                    //sendDataToSystemAdapter(RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(graphURI)}, data));
            });

        Entry<Long, Long> result = new SimpleEntry<>(recordCount.get(), batchCount.get());
        return result;
    }

}
