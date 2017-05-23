package org.hobbit;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.lang.PipedRDFIterator;
import org.apache.jena.riot.lang.PipedRDFStream;
import org.apache.jena.riot.lang.PipedTriplesStream;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by gkatsimpras on 28/2/2017.
 */
public class SampleDataGenerator extends AbstractDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleDataGenerator.class);
    Model rdfModel;
    private Semaphore generateTasks = new Semaphore(0);


    @Override
    public void init() throws Exception {
        // Always init the super class first!
        super.init();
        LOGGER.info("Data generator initialized");
        // Your initialization code comes here...
    }
    @Override
    protected void generateData() throws Exception {
        // Create your data inside this method. You might want to use the
        // id of this data generator and the number of all data generators
        // running in parallel.
        LOGGER.info("Starting data generation.");
        int dataGeneratorId = getGeneratorId();
        int numberOfGenerators = getNumberOfGenerators();
        List<Statement> stms = new ArrayList<>();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final InputStream tdOntology = classloader.getResourceAsStream("td.ttl");
        final InputStream trainingData = classloader.getResourceAsStream("training.ttl");
        List<InputStream> dataToAdd = Arrays.asList(tdOntology, trainingData);
        // large datasets must be splitted in smaller
        // create chunks
        for (final InputStream dataset : dataToAdd) {
            Model rdfModel = ModelFactory.createDefaultModel();
            PipedRDFIterator<Triple> iter = new PipedRDFIterator<>();
            final PipedRDFStream<Triple> inputStream = new PipedTriplesStream(iter);
            // PipedRDFStream and PipedRDFIterator need to be on different threads
            ExecutorService executor = Executors.newSingleThreadExecutor();
            // Create a runnable for our parser thread
            Runnable parser = new Runnable() {
                @Override
                public void run() {
                    // Call the parsing process.
                    RDFDataMgr.parse(inputStream, dataset, Lang.TURTLE);
                }
            };
            executor.submit(parser);
            while (iter.hasNext()) {
                Triple next = iter.next();
                //System.out.println("Triple");
                Statement dfd = rdfModel.asStatement(next);
                stms.add(dfd);
            }
            List<List<Statement>> stms_chunks = org.apache.commons.collections4.ListUtils.partition(stms, 10000);
            for (List<Statement> chunk : stms_chunks) {
                Model chunkRdfModel = ModelFactory.createDefaultModel();
            
                chunkRdfModel.add(chunk);
                //LOGGER.info("CModel: " + chunkRdfModel);
                byte[] data;
                // Create your data here

                // NEW VERSION
                StringWriter writer = new StringWriter();
                chunkRdfModel.write(writer, "N3");
                String dataString = writer.toString();

                data = RabbitMQUtils.writeString(dataString);


                // OLD VERSION
                // data = RabbitMQUtils.writeModel(chunkRdfModel);

                LOGGER.info("CONVERTED MODEL TO BYTE. SENDING TO TASKG");
                LOGGER.info("byte array: " + data);

                // NEW VERSION:
                String graphURI = "http://www.example.com/graph";
                byte[] graphInBytes = RabbitMQUtils.writeString(graphURI);
                byte[][] wrapper = {graphInBytes};
                byte[] dataWithGraph = RabbitMQUtils.writeByteArrays(null, wrapper, data);
                sendDataToSystemAdapter(dataWithGraph);

             
            }
        }
        sendToCmdQueue((byte)151);

        generateTasks.acquire();



    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        if (command == (byte) 150 ) {
            byte[] emptyByte = {};
            try {
                sendDataToTaskGenerator(emptyByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
            generateTasks.release();



        }
        super.receiveCommand(command, data);
    }


}

