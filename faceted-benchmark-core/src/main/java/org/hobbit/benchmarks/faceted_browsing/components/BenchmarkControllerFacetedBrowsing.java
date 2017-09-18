package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.hobbit.core.Commands;
import org.hobbit.interfaces.BenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

public class BenchmarkControllerFacetedBrowsing
    implements BenchmarkController
{
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkControllerFacetedBrowsing.class);

    @Resource(name="dataGeneratorServiceFactory")
    protected ServiceFactory<Service> dataGeneratorServiceFactory;

    @Resource(name="taskGeneratorServiceFactory")
    protected ServiceFactory<Service> taskGeneratorServiceFactory;

    @Resource(name="commandChannel")
    protected WritableByteChannel commandChannel;


    protected ServiceManager serviceManager;



    @Override
    public void init() throws Exception {
        logger.debug("Entered BenchmarkController::init()");

        Service dataGeneratorService = dataGeneratorServiceFactory.get();
        Service taskGeneratorService = taskGeneratorServiceFactory.get();

//        dataGeneratorService.startAsync();
//        taskGeneratorService.startAsync();

        serviceManager = new ServiceManager(Arrays.asList(
                dataGeneratorService,
                taskGeneratorService
        ));

        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(serviceManager,
                60, TimeUnit.SECONDS, 60, TimeUnit.SECONDS);

        logger.debug("Normally left BenchmarkController::init()");
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        logger.info("Seen command: " + command + " with " + data.length + " bytes");
    }

    @Override
    public void executeBenchmark() throws Exception {

        logger.info("Benchmark execution initiated");


        commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATOR_START_SIGNAL}));
        commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATOR_START_SIGNAL}));


        Thread.sleep(10000);

//
//        // wait for the data generators to finish their work
//        LOGGER.info("WAITING FOR DATA GENERATOR ...");
//        waitForDataGenToFinish();
//        // wait for the task generators to finish their work
//        // wait for the system to terminate
//        waitForTaskGenToFinish();
//        LOGGER.info("WAITING FOR SYSTEM ...");
//        waitForSystemToFinish();
//        this.stopContainer(containerName);
//
//
//        // Create the evaluation module
//        String evalModuleImageName = "git.project-hobbit.eu:4567/gkatsibras/facetedevaluationmodule/image";
//        String[] envVariables = new String[]{"NO_VAR=true"};
//        createEvaluationModule(evalModuleImageName, envVariables);
//        // wait for the evaluation to finish
//        waitForEvalComponentsToFinish();
//        // the evaluation module should have sent an RDF model containing the
//        // results. We should add the configuration of the benchmark to this
//        // model.
//        // this.resultModel.add(...);
//        // Send the resultModul to the platform controller and terminate
//        sendResultModel(resultModel);
    }

    @Override
    public void close() throws IOException {
        ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, 60, TimeUnit.SECONDS);
    }
}



