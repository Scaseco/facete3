package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.aksw.commons.collections.utils.StreamUtils;
import org.aksw.jena_sparql_api.utils.GraphUtils;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.hobbit.interfaces.DataGenerator;
import org.hobbit.interfaces.TripleStreamSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DataGeneratorFacetedBrowsing
    implements DataGenerator
{
    private static final Logger logger = LoggerFactory.getLogger(DataGeneratorFacetedBrowsing.class);

    protected int batchSize = 10000;

    @Resource
    protected TripleStreamSupplier tripleStreamSupplier;

    @Resource(name="dg2tg")
    protected WritableByteChannel toTaskGenerator;

    @Resource(name="dg2sa")
    protected WritableByteChannel toSystemAdatper;



    @Override
    public void init() throws Exception {

    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
//        streamManager.handleIncomingData(ByteBuffer.wrap(data));
    }

    @Override
    public void generateData() throws Exception {
        {
            Stream<Triple> triples = tripleStreamSupplier.get();
            sendTriples(triples, batchSize, toTaskGenerator);
        }

        {
            Stream<Triple> triples = tripleStreamSupplier.get();
            sendTriples(triples, batchSize, toSystemAdatper);
        }
    }

    public static Entry<Long, Long> sendTriples(Stream<Triple> stream, int batchSize, WritableByteChannel channel) {

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
                logger.info("CONVERTED MODEL TO BYTE. SENDING TO TASKG");
                logger.info("byte array: " + data);

                String graphURI = "http://www.example.com/graph";

                try {
                    channel.write(ByteBuffer.wrap(data));
                    //sink.accept(RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(graphURI)}, data));
                    //sendDataToSystemAdapter(RabbitMQUtils.writeByteArrays(null, new byte[][]{RabbitMQUtils.writeString(graphURI)}, data));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

        Entry<Long, Long> result = new SimpleEntry<>(recordCount.get(), batchCount.get());
        return result;
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

}
