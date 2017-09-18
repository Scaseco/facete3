package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.hobbit.interfaces.TaskGenerator;
import org.hobbit.transfer.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ServiceManager;

@Component
public class FacetedTaskGenerator
    implements TaskGenerator
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FacetedTaskGenerator.class);

    @Resource
    protected SparqlBasedSystemService preparationSparqlService;

    @Resource
    protected SparqlBasedSystemService evaluationSparqlService;

//    @Resource
//    protected

    protected StreamManager streamManager;

    @Override
    public void init() throws Exception {
        ServiceManager serviceManager = new ServiceManager(Arrays.asList(preparationSparqlService, evaluationSparqlService));

        try {
            // If we need to replay a large dataset, startup may take a while
            serviceManager.awaitHealthy(120, TimeUnit.SECONDS);
        } catch(Exception e) {
            LOGGER.error("Could not start the sparql service");
            serviceManager.stopAsync();
            serviceManager.awaitStopped(60, TimeUnit.SECONDS);
            throw new RuntimeException(e);
        }


        /**
         * The protocol here is:
         * We expect data to arrive exactly once in the form of a stream.
         *
         * This steam contains the dataset to be loaded into the preparation sparql endpoint
         *
         * Once the stream is consumed, the task generation starts.
         * The tasks are then evaluated against an evaluation sparqlService
         * and the result are set to the eval store.
         *
         * Finally, the tasks are sent again to system adapter
         *
         * As we have served out duty then, we can stop the services
         *
         */
        streamManager.registerCallback((in) -> {
            serviceManager.stopAsync();
            try {
                serviceManager.awaitStopped(60, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        });

        serviceManager.startAsync();
    }


    @Override
    public void receiveCommand(byte command, byte[] data) {
        streamManager.handleIncomingData(ByteBuffer.wrap(data));
    }

    /**
     * This method gets invoked by the data generator
     */
    @Override
    public void generateTask(byte[] data) throws Exception {
        streamManager.handleIncomingData(ByteBuffer.wrap(data));
    }

    @Override
    public void sendTaskToSystemAdapter(String taskIdString, byte[] data) throws IOException {
        // TODO Auto-generated method stub

    }
}
