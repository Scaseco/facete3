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
import org.hobbit.transfer.PublishingWritableByteChannelSimple;
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

    // This is the *local* publisher for the receiveCommand method
    protected PublishingWritableByteChannelSimple commandPublisher = new PublishingWritableByteChannelSimple();

    protected ServiceManager serviceManager;



    @Override
    public void init() throws Exception {
        logger.debug("Entered BenchmarkController::init()");

        Service dataGeneratorService = dataGeneratorServiceFactory.get();
        Service taskGeneratorService = taskGeneratorServiceFactory.get();

        serviceManager = new ServiceManager(Arrays.asList(
                dataGeneratorService,
                taskGeneratorService
        ));

        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(
                serviceManager,
                60, TimeUnit.SECONDS,
                60, TimeUnit.SECONDS);

        logger.debug("Normally left BenchmarkController::init()");
    }

    @Override
    public void receiveCommand(byte command, byte[] data) {
        logger.info("Seen command: " + command + " with " + data.length + " bytes");
        ByteBuffer bb = ByteBuffer.wrap(new byte[1 + data.length]).put(command).put(data);
        bb.rewind();

        try {
            commandPublisher.write(bb);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Predicate<ByteBuffer> firstByteEquals(byte b) {
        Predicate<ByteBuffer> result = buffer -> buffer.limit() > 0 && buffer.get(0) == b;
        return result;
    }

    @Override
    public void executeBenchmark() throws Exception {

        logger.info("Benchmark execution initiated");

        // Create the waits for the signals before sending the commands


//        commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATOR_START_SIGNAL}));
//        commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATOR_START_SIGNAL}));

        CompletableFuture<ByteBuffer> dataGenerationFuture = ByteChannelUtils.sendMessageAndAwaitResponse(
                commandChannel,
                ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATOR_START_SIGNAL}),
                Collections.singleton(commandPublisher),
                firstByteEquals(Commands.DATA_GENERATION_FINISHED));


        // Wait for data generation to finish


        // Wait


        CompletableFuture<ByteBuffer> taskGenerationFuture = ByteChannelUtils.sendMessageAndAwaitResponse(
                commandChannel,
                ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATOR_START_SIGNAL}),
                Collections.singleton(commandPublisher),
                firstByteEquals(Commands.TASK_GENERATION_FINISHED));

        CompletableFuture<?> preparationPhaseCompletion = CompletableFuture.allOf(dataGenerationFuture, taskGenerationFuture);

        try {
            preparationPhaseCompletion.get(60, TimeUnit.SECONDS);
        } catch(Exception e) {
            throw new RuntimeException("Preparation phase did not complete in time");
        }


        System.out.println("ACTUAL BENCHMARK BEGINS NOW");

//      commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATOR_START_SIGNAL}));




//        ByteChannelRequestFactorySimple requestFactory = new ByteChannelRequestFactorySimple(
//                commandChannel,
//                Arrays.asList(commandChannel));
//
        //requestFactory.sendRequestAndAwaitResponse(, timeout, unit)

        //CompletableFuture<ByteBuffer> future = PublisherUtils.awaitMessage(commandPublisher, firstByteEquals(Commands.DATA_GENERATOR_READY_SIGNAL));




        //CompletableFuture<ByteBuffer> ByteChannelUtils.awaitMessage(b -> Stream.of(b).map(b::array).test(b.length > 0).test());



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



