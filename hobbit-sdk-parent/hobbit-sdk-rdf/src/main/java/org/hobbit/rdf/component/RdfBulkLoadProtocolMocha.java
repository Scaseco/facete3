package org.hobbit.rdf.component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.rdfconnection.RDFConnection;
import org.hobbit.core.component.MochaConstants;
import org.hobbit.core.component.DataProtocol;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RdfBulkLoadProtocolMocha
	implements DataProtocol
{
	
	private static final Logger logger = LoggerFactory.getLogger(RdfBulkLoadProtocolMocha.class);

	
    protected boolean dataLoadingFinished = false;
    protected NavigableSet<String> graphUris = new TreeSet<String>(); 

    protected int counter = 0;

    protected AtomicInteger totalReceived = new AtomicInteger(0);
    protected AtomicInteger totalSent = new AtomicInteger(0);
    protected int loadingNumber = 0;
    protected String datasetFolderName = Files.createTempDir().getAbsolutePath();
	
	
	protected RDFConnection rdfConnection;
	protected Runnable onAllDataReceived;
	
	public RdfBulkLoadProtocolMocha(RDFConnection rdfConnection, Runnable onAllDataReceivedUserAction) {
		super();
		this.rdfConnection = rdfConnection;
		this.onAllDataReceived = () -> { dataLoadingFinished = true; onAllDataReceivedUserAction.run(); };
	}

	public void onData(ByteBuffer buf) throws IOException {
		ByteBuffer dataBuffer = buf.duplicate();    	
		if(dataLoadingFinished == false) {
			String fileName = RabbitMQUtils.readString(dataBuffer);
			
			logger.info("Receiving graph URI " + fileName);
			
			
			rdfConnection.update("CREATE SILENT GRAPH <" + fileName + ">");
			
			byte [] content = new byte[dataBuffer.remaining()];
			dataBuffer.get(content, 0, dataBuffer.remaining());

			if(content.length != 0) {
				if (fileName.contains("/")) {
					fileName = "file" + String.format("%010d", counter++) + ".ttl";
				}
				File file = new File(datasetFolderName + File.separator + fileName);

				try(FileOutputStream fos = new FileOutputStream(file)) {
					fos.write(content);
					fos.flush();
				}
				
				String finalFilename = file.getAbsolutePath();
				rdfConnection.load(fileName, finalFilename);
			}

			if(totalReceived.incrementAndGet() == totalSent.get()) {
				//allDataReceivedMutex.complete(null);
				onAllDataReceived.run();
			}
		}
		else {			
			String insertQuery = RabbitMQUtils.readString(dataBuffer);
			
			rdfConnection.update(insertQuery);
		}
	}	

    public void onCommand(ByteBuffer buf) {
    	ByteBuffer buffer = buf.duplicate();
    	if(!buffer.hasRemaining()) {
    		return;
    	}

    	int command = buffer.get();
    	
		if (MochaConstants.BULK_LOAD_DATA_GEN_FINISHED == command) {

			int numberOfMessages = buffer.getInt();
			boolean lastBulkLoad = buffer.get() != 0;

			if (lastBulkLoad) {
				dataLoadingFinished = true;
			}

			logger.info("Bulk loading phase (" + loadingNumber + ") begins");

			// if all data have been received before BULK_LOAD_DATA_GEN_FINISHED command received
			// release before acquire, so it can immediately proceed to bulk loading
			if(totalReceived.get() == totalSent.addAndGet(numberOfMessages)) {
				//allDataReceivedMutex.complete(null);
				onAllDataReceived.run();
			}

			loadingNumber++;
			
//			logger.info("Wait for receiving all data for bulk load " + loadingNumber + ".");
//			try {
//				allDataReceivedMutex.acquire();
//			} catch (InterruptedException e) {
//				logger.error("Exception while waitting for all data for bulk load " + loadingNumber + " to be recieved.", e);
//			}

		}
    }
}
