package org.hobbit;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Created by gkatsimpras on 28/2/2017.
 */
public class SampleDataGenerator extends AbstractDataGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleDataGenerator.class);
    Model rdfModel;

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
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream trainingData = classloader.getResourceAsStream("TrainingData.ttl");
        // large datasets must be splitted in smaller
        // create chunks
        Model rdfModel = ModelFactory.createDefaultModel();
        rdfModel.read(trainingData, null, "TTL");
        byte[] data;
        // Create your data here
        data = RabbitMQUtils.writeModel(rdfModel);
        LOGGER.info("CONVERTED MODEL TO BYTE. SENDING TO TASKG");
        // the data can be sent to the task generator(s) ...
        sendDataToTaskGenerator(data);
        // ... and/or to the system
        sendDataToSystemAdapter(data);
    }


}
