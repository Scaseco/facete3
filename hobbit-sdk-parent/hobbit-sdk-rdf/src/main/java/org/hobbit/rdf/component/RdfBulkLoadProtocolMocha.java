package org.hobbit.rdf.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.jena_sparql_api.core.utils.ServiceUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.query.QueryExecution;
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
	protected Runnable onAllDataReceivedUserAction;
	
	
	protected Deque<Integer> pendingBulkLoadSizes = new ArrayDeque<>();
	protected boolean lastBulkLoadSeen = false;
	
	// If non-null, remap requests to this graph to the given one.
	// Used to hack around the fact that virtuos's graph store protocol has the default graph disabled by default
	protected String remappedDefaultGraph;
	
	public static boolean isDefaultGraph(String graphName) {
		boolean result = graphName == null || graphName.equals("") || graphName.equals("default") || graphName.startsWith("default");
		return result;
	}
	
	public RdfBulkLoadProtocolMocha(RDFConnection rdfConnection, String remappedDefaultGraph ,Runnable postLoad, Runnable onAllDataReceivedUserAction) {
		super();
		this.rdfConnection = rdfConnection;
		this.remappedDefaultGraph = remappedDefaultGraph;
		
		this.postLoad = postLoad;
		this.onAllDataReceivedUserAction = onAllDataReceivedUserAction;
	}
	
	protected void onAllDataReceived() {
		logger.info("All data received - bulk loading phase over.");

		dataLoadingFinished = true;
		onAllDataReceivedUserAction.run();
	}
	
	public synchronized void checkWhetherToProcessBatch() {
		while(!pendingBulkLoadSizes.isEmpty()) {
			int requiredMessageCountForBatch = pendingBulkLoadSizes.peekFirst();
			
			// Check if there are enough messages present
			int availableMessageCount = graphToFiles.size();
			
			if(availableMessageCount >= requiredMessageCountForBatch) {
				logger.info("Bulk loading phase (" + loadingNumber + ") begins; data: " + graphToFiles);

				pendingBulkLoadSizes.removeFirst();
				
				Set<String> graphNames = new HashSet<>();
				for(int i = 0; i < requiredMessageCountForBatch; ++i) {
					Entry<String, File> e = graphToFiles.pollFirst();
					String graph = e.getKey();
					String finalFilename = e.getValue().getAbsolutePath();
					
					//rdfConnection.begin(ReadWrite.WRITE);
					//rdfConnection.update("CREATE SILENT GRAPH <" + graph + ">");

					//System.out.println("RDF conn is " + rdfConnection);
					//String loadGraph = "default".equals(graph) ? null : graph;
					String finalGraphName = isDefaultGraph(graph)
							? remappedDefaultGraph
							: graph;

	                graphNames.add(finalGraphName);

	                logger.info("Requesting to load file " + finalFilename + " into " + finalGraphName);
					rdfConnection.load(finalGraphName, finalFilename);
					//rdfConnection.load(finalFilename);
					//rdfConnection.commit();
				}

				//if(("" + rdfConnection).contains("Remote")) {
					//logger.info("Debug point: " + rdfConnection);
				//}
				for(String graph : graphNames) {
//				    if(isDefaultGraph(graph)) {
//				        graph = null;
//				    }

					try(QueryExecution qe = rdfConnection.query("SELECT (COUNT(*) AS ?c) { " + (graph == null ? "" : "GRAPH <" + graph + "> { ") + "?s ?p ?o" + (graph == null ? "" : " } ") + " }")) {
						Integer count = ServiceUtils.fetchInteger(qe, Vars.c);
						
						logger.info("Counted " + count + " triples in graph " + graph);
						//logger.info(ResultSetFormatter.asText(qe.execSelect()));
					}
				}
				
				loadingNumber++;

				postLoad.run();
			} else {
				break;
			}
		}
		
		if (pendingBulkLoadSizes.isEmpty() && lastBulkLoadSeen) {
			onAllDataReceived();
		} else {
			//logger.info("Bulk load is still waiting for more data");
		}
	}

	public synchronized void onData(ByteBuffer buf) throws IOException {
		ByteBuffer dataBuffer = buf.duplicate();	
		if(!dataLoadingFinished) {
			String graphUri = RabbitMQUtils.readString(dataBuffer);
			
			String filename = graphUri;
			//logger.info("Receiving graph URI " + filename);

			byte [] content = new byte[dataBuffer.remaining()];
			dataBuffer.get(content, 0, dataBuffer.remaining());

			if(content.length != 0) {
				//if (filename.contains("/")) {
					//filename = "file" + String.format("%010d", counter++) + ".ttl";
					// .ttl suffix needed for jena to automatically recognize the format
					filename = filename.replaceAll("[^/]*[/]", "");//+ ".ttl";
				//}
				//filename += ".ttl";
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
