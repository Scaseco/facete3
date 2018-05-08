package org.hobbit.rdf.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdfconnection.RDFConnection;
import org.hobbit.core.component.DataProtocol;
import org.hobbit.core.component.MochaConstants;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol implementation which assumes data messages to arrive in order
 * but control messages on a separate channel.
 * 
 * @author raven Feb 15, 2018
 *
 */
public class RdfBulkLoadProtocolMocha
	implements DataProtocol
{
	
	private static final Logger logger = LoggerFactory.getLogger(RdfBulkLoadProtocolMocha.class);

	
    protected boolean dataLoadingFinished = false;
    //protected NavigableSet<String> graphUris = new TreeSet<String>(); 

    protected Deque<Entry<String, File>> graphToFiles = new ArrayDeque<>(); //LinkedListMultimap.create();
    
    protected int counter = 0;

//    protected AtomicInteger totalReceived = new AtomicInteger(0);
//    protected AtomicInteger totalSent = new AtomicInteger(0);
    protected int loadingNumber = 0;
    protected String datasetFolderName = Files.createTempDir().getAbsolutePath();
	
	
	protected RDFConnection rdfConnection;
	protected Runnable postLoad;
	protected Runnable onAllDataReceived;
	
	
	protected Deque<Integer> pendingBulkLoadSizes = new ArrayDeque<>();
	protected boolean lastBulkLoadSeen = false;
	
	public RdfBulkLoadProtocolMocha(RDFConnection rdfConnection, Runnable postLoad, Runnable onAllDataReceivedUserAction) {
		super();
		this.rdfConnection = rdfConnection;
		this.postLoad = postLoad;

		this.onAllDataReceived = () -> {
			dataLoadingFinished = true;
			onAllDataReceivedUserAction.run();
		};
	}
	
	public synchronized void checkWhetherToProcessBatch() {
		while(!pendingBulkLoadSizes.isEmpty()) {
			int requiredMessageCountForBatch = pendingBulkLoadSizes.peekFirst();
			
			// Check if there are enough messages present
			int availableMessageCount = graphToFiles.size();
			
			if(availableMessageCount >= requiredMessageCountForBatch) {
				logger.info("Bulk loading phase (" + loadingNumber + ") begins; data: " + graphToFiles);

				pendingBulkLoadSizes.removeFirst();
				
				for(int i = 0; i < requiredMessageCountForBatch; ++i) {
					Entry<String, File> e = graphToFiles.pollFirst();
					String graph = e.getKey();
					String finalFilename = e.getValue().getAbsolutePath();

					rdfConnection.update("CREATE SILENT GRAPH <" + graph + ">");

					//System.out.println("RDF conn is " + rdfConnection);
					rdfConnection.load(graph, finalFilename);
				}

				//if(("" + rdfConnection).contains("Remote")) {
					logger.info("Debug point: " + rdfConnection);
				//}
				try(QueryExecution qe = rdfConnection.query("SELECT (COUNT(*) AS ?c) { GRAPH ?g { ?s ?p ?o } }")) {
					logger.info(ResultSetFormatter.asText(qe.execSelect()));
				}
				
				loadingNumber++;

				postLoad.run();
			} else {
				break;
			}
		}
		
		if (pendingBulkLoadSizes.isEmpty() && lastBulkLoadSeen) {
			onAllDataReceived.run();
		}
	}

	public synchronized void onData(ByteBuffer buf) throws IOException {
		ByteBuffer dataBuffer = buf.duplicate();	
		if(!dataLoadingFinished) {
			String graphUri = RabbitMQUtils.readString(dataBuffer);
			
			String filename = graphUri;
			logger.info("Receiving graph URI " + filename);

			byte [] content = new byte[dataBuffer.remaining()];
			dataBuffer.get(content, 0, dataBuffer.remaining());

			if(content.length != 0) {
				if (filename.contains("/")) {
					filename = "file" + String.format("%010d", counter++) + ".ttl";
				}
				File file = new File(datasetFolderName + File.separator + filename);

				try(FileOutputStream fos = new FileOutputStream(file)) {
					fos.write(content);
					fos.flush();
				}
				
				graphToFiles.addLast(new SimpleEntry<>(graphUri, file));
				
				//postLoad.run();
			}
			
			checkWhetherToProcessBatch();
		}
		else {			
			String insertQuery = RabbitMQUtils.readString(dataBuffer);
			
			rdfConnection.update(insertQuery);
		}
	}	

    public synchronized void onCommand(ByteBuffer buf) {
    	ByteBuffer buffer = buf.duplicate();
    	if(!buffer.hasRemaining()) {
    		return;
    	}

    	int command = buffer.get();
    	
		if (MochaConstants.BULK_LOAD_DATA_GEN_FINISHED == command) {
			
			int numberOfMessages = buffer.getInt();
			boolean lastBulkLoad = buffer.get() != 0;

			if(lastBulkLoadSeen) {
				throw new RuntimeException("Receiving a bulk loading message although a prior message indicated that no more messages of this type will be sent");
			}
			
			lastBulkLoadSeen = lastBulkLoadSeen || lastBulkLoad;
			pendingBulkLoadSizes.add(numberOfMessages);
			
			checkWhetherToProcessBatch();
		}
    }
}
