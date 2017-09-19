package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.annotation.Resource;

import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdfconnection.RDFConnection;
import org.hobbit.core.Commands;
import org.hobbit.transfer.InputStreamManagerImpl;
import org.hobbit.transfer.Publisher;
import org.hobbit.transfer.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

@Component
public class TaskGeneratorFacetedBenchmark
    extends ComponentBase
{
    private static final Logger logger = LoggerFactory.getLogger(TaskGeneratorFacetedBenchmark.class);

    @Resource(name="preparationSparqlService")
    protected SparqlBasedSystemService preparationSparqlService;

    @Resource(name="commandChannel")
    protected WritableByteChannel commandChannel;

//    @Resource(name="dataChannel")
//    protected WritableByteChannel dataChannel;

    @Resource(name="dg2tg")
    protected Publisher<ByteBuffer> fromDataGenerator;

    @Resource(name="tg2sa")
    protected WritableByteChannel toSystemAdater;


    //@Resource(name="referenceSparqlService")
    //protected SparqlBasedSystemService referenceSparqlService;

//    @Resource
//    protected
    protected ServiceManager serviceManager;

    protected StreamManager streamManager;


    // The generated tasks; we should use file persistence for scaling in the general case
    protected Collection<String> generatedTasks = new ArrayList<>();

    @Override
    public void init() throws Exception {
        // Avoid duplicate services
        Set<Service> services = Sets.newIdentityHashSet();
        services.addAll(Arrays.asList(
                preparationSparqlService
                //referenceSparqlService
        ));

        serviceManager = new ServiceManager(services);

        streamManager = new InputStreamManagerImpl(commandChannel);

        //Consumer<ByteBuffer> fromDataGeneratorObserver
        fromDataGenerator.subscribe(streamManager::handleIncomingData);

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
        streamManager.subscribe((in) -> {
            logger.debug("Data stream from data generator received");

            RDFConnection conn = preparationSparqlService.createDefaultConnection();
            try {
                // Perform bulk load
                File tmpFile = File.createTempFile("hobbit-faceted-browsing-benchmark-task-generator-bulk-load-", ".nt");
                tmpFile.deleteOnExit();
                FileCopyUtils.copy(in, new FileOutputStream(tmpFile));

                // TODO Bulk loading not yet implemented...

                //conn.load("http://www.example.com/graph", tmpFile.getAbsolutePath());
                tmpFile.delete();
            } catch(Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            logger.debug("Bulk loading phase complete, starting task generation");

            // Now invoke the actual task generation
            FacetedTaskGeneratorOld gen = new FacetedTaskGeneratorOld();

            gen.setQueryConn(conn);
            gen.initializeParameters();
            Stream<String> tasks = gen.generateTasks();

            tasks.forEach(task -> {
                System.out.println("Generated task: " + task);
                generatedTasks.add(task);
            });


            ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, 60, TimeUnit.SECONDS);


            try {
                commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATION_FINISHED}));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        });


        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(serviceManager,
                60, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);


        commandPublisher.subscribe((buffer) -> {
            streamManager.handleIncomingData(buffer.duplicate());

            if(buffer.hasRemaining()) {
                byte cmd = buffer.get(0);
                if(cmd == BenchmarkControllerFacetedBrowsing.START_BENCHMARK_SIGNAL) {
                    sendOutTasksToSystemAdapter();
                }
            }
        });

    }


    protected void sendOutTasksToSystemAdapter() {
        try(Stream<String> taskStream = generatedTasks.stream()) {

            // Pretend we have a stream of tasks because this is what it should eventually be
            taskStream.forEach(task -> {
                try {
                    toSystemAdater.write(ByteBuffer.wrap(task.getBytes(StandardCharsets.UTF_8)));
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }


//    @Override
//    public void receiveCommand(byte command, byte[] data) {
//        streamManager.handleIncomingData(ByteBuffer.wrap(data));
//    }

//    /**
//     * This method gets invoked by the data generator
//     */
//    @Override
//    public void generateTask(byte[] data) throws Exception {
//        streamManager.handleIncomingData(ByteBuffer.wrap(data));
//    }
//
//    @Override
//    public void sendTaskToSystemAdapter(String taskIdString, byte[] data) throws IOException {
//        // TODO Auto-generated method stub
//
//    }

    @Override
    public void close() throws IOException {
        streamManager.close();
        ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, 60, TimeUnit.SECONDS);

        fromDataGenerator.unsubscribe(streamManager::handleIncomingData);
    }
}
