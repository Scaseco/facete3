package org.hobbit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.apache.jena.atlas.web.TypedInputStream;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Streams;



/**
 * Revised by Claus Stadler 14/09/2017.
 * Created by gkatsimpras on 28/2/2017.
 */
public class DataGeneratorFacetedBrowsingBenchmark extends AbstractDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataGeneratorFacetedBrowsingBenchmark.class);

    protected Supplier<Stream<Triple>> tripleStreamSupplier;
    protected int batchSize = 10000;

    @Override
    public void init() throws Exception {
        super.init();
        LOGGER.info("Data generator initialized");
    }

    @Override
    protected void generateData() throws Exception {
        LOGGER.info("Starting data generation.");

        //List<String> resourceNames = Arrays.asList("td-ontology.ttl", "TrainingData.ttl");

        long batchCount = 0;
        //for(String resourceName : resourceNames) {

            LOGGER.info("  Attempting to load static data resource: " + resourceName);
            TypedInputStream in = RDFDataMgr.open(resourceName);
            Lang lang = RDFDataMgr.determineLang(resourceName, in.getContentType(), Lang.TURTLE);
            Iterator<Triple> it = RDFDataMgr.createIteratorTriples(in, lang, "http://example.org/");

            Entry<Long, Long> recordAndChunkCounts = this.sendTriples(it, batchSize);

            batchCount += recordAndChunkCounts.getKey();
        //}

        sendToCmdQueue((byte)151, RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeLong(batchCount)}, RabbitMQUtils.writeLong(1)));
    }


    public Entry<Long, Long> sendTriples(Iterator<Triple> it, int batchSize) {

        AtomicLong recordCount = new AtomicLong();
        AtomicLong batchCount = new AtomicLong();

        Stream<Triple> stream = Streams.stream(it);


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
                    sendDataToSystemAdapter(RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(graphURI)}, data));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        Entry<Long, Long> result = new SimpleEntry<>(recordCount.get(), batchCount.get());
        return result;
    }


    @Override
    public void receiveCommand(byte command, byte[] data) {
        if (command == (byte) 150 ) {
            byte[] emptyByte = {};
            try {
                // TODO Using an empty message to trigger actions looks scary to me ~ Claus
                // to invoke the task generator to start
                sendDataToTaskGenerator(emptyByte);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.receiveCommand(command, data);
    }

}

