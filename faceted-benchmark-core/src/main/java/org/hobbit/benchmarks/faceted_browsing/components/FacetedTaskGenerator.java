package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.hobbit.interfaces.TaskGenerator;
import org.hobbit.transfer.InputStreamManagerImpl;
import org.hobbit.transfer.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

@Component
public class FacetedTaskGenerator
    implements TaskGenerator
{
    private static final Logger logger = LoggerFactory.getLogger(FacetedTaskGenerator.class);

    @Resource(name="preparationSparqlService")
    protected SparqlBasedSystemService preparationSparqlService;

    @Resource(name="commandChannel")
    protected WritableByteChannel commandChannel;

    @Resource(name="dataChannel")
    protected WritableByteChannel dataChannel;

    //@Resource(name="referenceSparqlService")
    //protected SparqlBasedSystemService referenceSparqlService;

//    @Resource
//    protected
    protected ServiceManager serviceManager;

    protected StreamManager streamManager;

    @Override
    public void init() throws Exception {
        // Avoid duplicate services
        Set<Service> services = Sets.newIdentityHashSet();
        services.addAll(Arrays.asList(
                preparationSparqlService
                //referenceSparqlService
        ));

        serviceManager = new ServiceManager(services);

        streamManager = new InputStreamManagerImpl(dataChannel);

        /*
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


        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(serviceManager,
                60, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);

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

    @Override
    public void close() throws IOException {
        serviceManager.stopAsync();
        try {
            serviceManager.awaitStopped(60, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
