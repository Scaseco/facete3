package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import javax.annotation.Resource;

import org.hobbit.core.Commands;
import org.hobbit.interfaces.BenchmarkController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

public class BenchmarkControllerFacetedBrowsing
    extends ComponentBase
    implements BenchmarkController
{
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkControllerFacetedBrowsing.class);

    @Resource(name="dataGeneratorServiceFactory")
    protected ServiceFactory<Service> dataGeneratorServiceFactory;

    @Resource(name="taskGeneratorServiceFactory")
    protected ServiceFactory<Service> taskGeneratorServiceFactory;

    @Resource(name="systemAdapterServiceFactory")
    protected ServiceFactory<Service> systemAdapterServiceFactory;

    @Resource(name="evaluationModuleServiceFactory")
    protected ServiceFactory<Service> evaluationModuleServiceFactory;


    @Resource(name="commandChannel")
    protected WritableByteChannel commandChannel;

    protected ServiceManager serviceManager;



    protected CompletableFuture<ByteBuffer> systemUnderTestReadyFuture;
    protected Service dataGeneratorService;
    protected Service taskGeneratorService;
    protected Service systemAdapterService;

    public static final byte START_BENCHMARK_SIGNAL = 66;

    @Override
    public void init() throws Exception {
        logger.debug("Entered BenchmarkController::init()");

        // The system adapter will send a ready signal, hence register on it on the command queue before starting the service
        // NOTE A completable future will resolve only once; Java 9 flows would allow multiple resolution (reactive streams)
        systemUnderTestReadyFuture = PublisherUtils.awaitMessage(commandPublisher,
                firstByteEquals(Commands.SYSTEM_READY_SIGNAL));

        dataGeneratorService = dataGeneratorServiceFactory.get();
        taskGeneratorService = taskGeneratorServiceFactory.get();
        systemAdapterService = systemAdapterServiceFactory.get();

        serviceManager = new ServiceManager(Arrays.asList(
                dataGeneratorService,
                taskGeneratorService,
                systemAdapterService
        ));

        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(
                serviceManager,
                60, TimeUnit.SECONDS,
                60, TimeUnit.SECONDS);

        logger.debug("Normally left BenchmarkController::init()");
    }


    public Predicate<ByteBuffer> firstByteEquals(byte b) {
        Predicate<ByteBuffer> result = buffer -> buffer.limit() > 0 && buffer.get(0) == b;
        return result;
    }

    @Override
    public void executeBenchmark() throws Exception {

        logger.info("Benchmark execution initiated");

        CompletableFuture<ByteBuffer> dataGenerationFuture = ByteChannelUtils.sendMessageAndAwaitResponse(
                commandChannel,
                ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATOR_START_SIGNAL}),
                Collections.singleton(commandPublisher),
                firstByteEquals(Commands.DATA_GENERATION_FINISHED));

        CompletableFuture<ByteBuffer> taskGenerationFuture = ByteChannelUtils.sendMessageAndAwaitResponse(
                commandChannel,
                ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATOR_START_SIGNAL}),
                Collections.singleton(commandPublisher),
                firstByteEquals(Commands.TASK_GENERATION_FINISHED));

        // Wait for the system-under-test to report its ready state
        //CompletableFuture<ByteBuffer> taskGenerationFuture = ByteChannelUtils.sen

        CompletableFuture<?> preparationPhaseCompletion = CompletableFuture.allOf(
                dataGenerationFuture,
                taskGenerationFuture,
                systemUnderTestReadyFuture);

        try {
            preparationPhaseCompletion.get(60, TimeUnit.SECONDS);
        } catch(Exception e) {
            throw new RuntimeException("Preparation phase did not complete in time", e);
        }


        System.out.println("ACTUAL BENCHMARK BEGINS NOW");

        // Instruct the task generator(s) to run their tasks
        commandChannel.write(ByteBuffer.wrap(new byte[]{START_BENCHMARK_SIGNAL}));


        // Stop unneeded services to free resources
        dataGeneratorService.stopAsync();





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



