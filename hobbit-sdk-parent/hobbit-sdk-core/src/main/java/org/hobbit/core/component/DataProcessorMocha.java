package org.hobbit.core.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.collections.cache.CountingIterator;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;

import io.reactivex.Flowable;


/**
 * Send data via the mocha protocol to a TG and a SA
 * 
 * @author raven
 *
 */
public class DataProcessorMocha
	implements DataSink<Triple>
{
	public static final Logger logger = LoggerFactory.getLogger(DataProcessorMocha.class);

    protected Subscriber<ByteBuffer> toTaskGenerator;
    protected Subscriber<ByteBuffer> toSystemAdatper;
    protected Subscriber<ByteBuffer> commandSender;

    protected int batchSize = 10000;
    
	public DataProcessorMocha(Subscriber<ByteBuffer> toTaskGenerator, Subscriber<ByteBuffer> toSystemAdatper,
			Subscriber<ByteBuffer> commandSender, int batchSize) {
		super();
		this.toTaskGenerator = toTaskGenerator;
		this.toSystemAdatper = toSystemAdatper;
		this.commandSender = commandSender;
		this.batchSize = batchSize;
	}


	@Override
	public void accept(Supplier<Flowable<Triple>> tripleStreamSupplier) {
		try {
			process(tripleStreamSupplier);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void process(Supplier<Flowable<Triple>> tripleStreamSupplier) throws IOException {
	        // Generate the data once and store it in a temp file
	        // TODO Add a flag whether to cache in a file
	        File datasetFile = File.createTempFile("hobbit-data-generator-faceted-browsing-", ".nt");
	        
	        // TODO Enable an optional validation step which keeps the output files if there are any problems
	        
	        datasetFile.deleteOnExit();
//	        Flowable<?> flowable = flowableSupplier.get();
//	        flowable.blockingIterable().iterator();
	        
	        Flowable<Triple> flowable = tripleStreamSupplier.get();
	        Iterable<Triple> triples = flowable.blockingIterable();
	        
        	CountingIterator<Triple> it = new CountingIterator<>(triples.iterator());
        	
        	RDFDataMgr.writeTriples(new FileOutputStream(datasetFile), it);
        	logger.info("Data generator counted " + it.getNumItems() + " generated triples");
	        
	        Supplier<Stream<Triple>> triplesFromCache = () -> {
	            try {
	                return Streams.stream(RDFDataMgr.createIteratorTriples(new FileInputStream(datasetFile), Lang.NTRIPLES, "http://example.org/"));
	            } catch (FileNotFoundException e) {
	                throw new RuntimeException(e);
	            }
	        };

	        
	        //commandSender.onNext((ByteBuffer)ByteBuffer.allocate(1).put(MochaConstants.BULK_LOAD_FROM_DATAGENERATOR).rewind());
	        
	        // TODO We should run this in parallel in production mode
	        
	        logger.info("Data generator is sending dataset to task generator");
	        DataGeneratorComponentBase.sendTriplesViaMochaProtocol(triplesFromCache.get(), batchSize, toTaskGenerator::onNext);
	        //sendTriplesViaStreamProtocol(triplesFromCache.get(), batchSize, toTaskGenerator::onNext);
	        
	        logger.info("Data generator is sending dataset to system adapter");
	        Entry<Long, Long> numRecordsAndBatches = DataGeneratorComponentBase.sendTriplesViaMochaProtocol(triplesFromCache.get(), batchSize, toSystemAdatper::onNext);

	    	// Notify the BC about this DC's contribution to the dataset generation
	    	commandSender.onNext((ByteBuffer)ByteBuffer.allocate(17)
	    			.put(MochaConstants.BULK_LOAD_FROM_DATAGENERATOR)
	    			.putLong(numRecordsAndBatches.getKey())
	    			.putLong(numRecordsAndBatches.getValue())
	    			.rewind());

	        datasetFile.delete();	
	}
}
