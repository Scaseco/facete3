package org.hobbit.core.component;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.hobbit.core.Commands;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.service.api.RunnableServiceCapable;
import org.hobbit.core.service.api.ServiceBuilder;
import org.hobbit.core.utils.ByteChannelUtils;
import org.hobbit.core.utils.PublisherUtils;
import org.hobbit.core.utils.ServiceManagerUtils;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import com.google.common.util.concurrent.ServiceManager;

@Component
public class BenchmarkControllerFacetedBrowsing
    extends ComponentBase
    implements RunnableServiceCapable
    //implements BenchmarkController
{
    private static final Logger logger = LoggerFactory.getLogger(BenchmarkControllerFacetedBrowsing.class);

    @Resource(name="dataGeneratorServiceFactory")
    protected ServiceBuilder<Service> dataGeneratorServiceFactory;

    @Resource(name="taskGeneratorServiceFactory")
    protected ServiceBuilder<Service> taskGeneratorServiceFactory;

//    @Resource(name="systemAdapterServiceFactory")
//    protected ServiceFactory<Service> systemAdapterServiceFactory;

    @Resource(name="evaluationStorageServiceFactory")
    protected ServiceBuilder<Service> evaluationStorageServiceFactory;

    @Resource(name="evaluationModuleServiceFactory")
    protected ServiceBuilder<Service> evaluationModuleServiceFactory;


    // Issue: How to get the result from the evaluation module?


    @Resource(name="commandSender")
    protected Subscriber<ByteBuffer> commandSender;

    protected ServiceManager serviceManager;



    //protected CompletableFuture<ByteBuffer> systemUnderTestReadyFuture;

    // FIXME HACK - A service should not be considered started unless they are ready 
    protected CompletableFuture<ByteBuffer> dataGeneratorReadyFuture;
    protected CompletableFuture<ByteBuffer> taskGeneratorReadyFuture;


    // The future for whether the evaluation data has been received
    protected CompletableFuture<ByteBuffer> evaluationDataReceivedFuture;


    protected Service dataGeneratorService;
    protected Service taskGeneratorService;
    protected Service systemAdapterService;

    protected Service evaluationStorageService;
    protected Service evaluationModuleService;


    protected CompletableFuture<State> dataGenerationTerminatedFuture;
    protected CompletableFuture<State> taskGenerationTerminatedFuture;

    //public static final byte START_BENCHMARK_SIGNAL = 66;


    @Override
    public void startUp() throws Exception {
        logger.debug("BenchmarkController::startUp()");

        // The system adapter will send a ready signal, hence register on it on the command queue before starting the service
        // NOTE A completable future will resolve only once; Java 9 flows would allow multiple resolution (reactive streams)
//        systemUnderTestReadyFuture = PublisherUtils.triggerOnMessage(commandPublisher,
//                ByteChannelUtils.firstByteEquals(Commands.SYSTEM_READY_SIGNAL));

        dataGeneratorReadyFuture = PublisherUtils.triggerOnMessage(commandPublisher,
                ByteChannelUtils.firstByteEquals(Commands.DATA_GENERATOR_READY_SIGNAL));

        taskGeneratorReadyFuture = PublisherUtils.triggerOnMessage(commandPublisher,
                ByteChannelUtils.firstByteEquals(Commands.TASK_GENERATOR_READY_SIGNAL));

        
        dataGeneratorService = dataGeneratorServiceFactory.get();
        taskGeneratorService = taskGeneratorServiceFactory.get();
        //systemAdapterService = systemAdapterServiceFactory.get();
        evaluationStorageService = evaluationStorageServiceFactory.get();
        evaluationModuleService = evaluationModuleServiceFactory.get();


        // - The condition to determine the end of a benchmark run is, that the task generator(s) have shut down
        // - The *BC* (NOT THE TG) has then to send out the TASK_GENERATION_FINISHED event!
//        taskGeneratorService.addListener(new Listener() {
//            @Override
//            public void terminated(State from) {
//                try {
//                    commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATION_FINISHED}));
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                super.terminated(from);
//            }
//        }, MoreExecutors.directExecutor());


        dataGenerationTerminatedFuture = ServiceManagerUtils.awaitState(dataGeneratorService, State.TERMINATED);
        
        taskGenerationTerminatedFuture = ServiceManagerUtils.awaitState(taskGeneratorService, State.TERMINATED);

        //systemAdapterTerminatedFuture = ServiceManagerUtils.awaitState(systemAdapterService, State.TERMINATED);


        taskGenerationTerminatedFuture.whenComplete((v, t) -> {
//            try {
                logger.debug("Sending out task TASK_GENERATION_FINISHED signal");
                commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATION_FINISHED}));
//            } catch(IOException e) {
//                throw new RuntimeException(e);
//            }
        });

        
        evaluationDataReceivedFuture = PublisherUtils.triggerOnMessage(commandPublisher, ByteChannelUtils.firstByteEquals(Commands.EVAL_MODULE_FINISHED_SIGNAL));

        evaluationDataReceivedFuture = evaluationDataReceivedFuture.whenComplete((buffer, ex) -> {
            logger.debug("Evaluation model received");
            Model model = RabbitMQUtils.readModel(buffer.array(), 1, buffer.limit() - 1);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            RDFDataMgr.write(baos, model, Lang.NTRIPLES);
            String str = baos.toString();
            logger.debug("Received eval model is: " + str);
        });
        
        serviceManager = new ServiceManager(Arrays.asList(
                dataGeneratorService,
                taskGeneratorService,
                //systemAdapterService,
                evaluationStorageService
                //evaluationModuleService
        ));


        logger.info("BenchmarkController::startUp() Waiting for services to start...");
        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(
                serviceManager,
                60, TimeUnit.SECONDS,
                60, TimeUnit.SECONDS);

        logger.info("BenchmarkController::startUp() completed.");
        
//        commandPublisher.subscribe(buffer -> {
//            if(buffer.hasRemaining()) {
//                byte cmd = buffer.get();
//                switch(cmd) {
//                case Commands.EVAL_MODULE_FINISHED_SIGNAL: {
//                    setResultModel(RabbitMQUtils.readModel(buffer.array(), 1, buffer.remaining()));
//                    logger.info("model size = " + resultModel.size());
//
//
//                }
//            }
//        });
    }

    @Override
    public void run() throws Exception {



        logger.info("Waiting for data and task generators to become ready");
        CompletableFuture<?> initFuture = CompletableFuture.allOf(dataGeneratorReadyFuture, taskGeneratorReadyFuture);
        initFuture.get(60, TimeUnit.SECONDS);
        
        
        logger.info("Benchmark execution initiated");

        /*
         * Issue: Once the data generator is started, (preprational) data is sent to the task generator
         *
         *
         */
//        CompletableFuture<ByteBuffer> dataGenerationFuture = ByteChannelUtils.sendMessageAndAwaitResponse(
//                commandChannel,
//                new byte[]{Commands.DATA_GENERATOR_START_SIGNAL},
//                commandPublisher,
//                ByteChannelUtils.firstByteEquals(Commands.DATA_GENERATION_FINISHED));


        // Wait for the task generation service to stop

        // Send out the data generation start signal

        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATOR_START_SIGNAL}));

        

//        CompletableFuture<ByteBuffer> taskGenerationFuture = ByteChannelUtils.sendMessageAndAwaitResponse(
//                commandChannel,
//                new byte[]{Commands.TASK_GENERATOR_START_SIGNAL},
//                commandPublisher,
//                ByteChannelUtils.firstByteEquals(Commands.TASK_GENERATION_FINISHED));

        // Wait for the system-under-test to report its ready state
        //CompletableFuture<ByteBuffer> taskGenerationFuture = ByteChannelUtils.sen


        // FIXME Actually we only need to wait for the ready signals of the task and data generator
        // The service stuff is just further info


        logger.debug("Waiting for data generation phase to complete");
        CompletableFuture<?> dataGenerationPhaseCompletion = CompletableFuture.allOf(
                dataGenerationTerminatedFuture);
                //systemUnderTestReadyFuture);

        try {
            dataGenerationPhaseCompletion.get(180, TimeUnit.SECONDS);
        } catch(Exception e) {
            throw new RuntimeException("Data generation phase did not complete in time", e);
        }

        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATION_FINISHED}));

        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATOR_START_SIGNAL}));



        logger.info("Waiting for task generation phase to complete");
        CompletableFuture<?> taskGenerationPhaseCompletion = taskGenerationTerminatedFuture;
                //CompletableFuture.allOf(dataGenerationFuture);

        try {
            taskGenerationPhaseCompletion.get(5, TimeUnit.MINUTES);
        } catch(Exception e) {
            throw new RuntimeException("Task generation phase did not complete in time", e);
        }


        logger.info("ACTUAL BENCHMARK BEGINS NOW");


        //commandChannel.onNext(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATION_FINISHED}));



        // Instruct the task generator(s) to run their tasks
        // FIXME This is just my ad-hoc signal
        //commandChannel.write(ByteBuffer.wrap(new byte[]{START_BENCHMARK_SIGNAL}));




        // Stop unneeded services to free resources
        // dataGeneratorService.stopAsync();


        // Wait for the benchmark run to finish
        // This is indicated by the task generator service
        // shutting itself down; hence we just have to wait here
        // taskGeneratorService.awaitTerminated(60, TimeUnit.SECONDS);


        // The evaluation module will immediately on start request the data from the eval store
        // so for things to work correctly, the eval store's data must be complete
        
        
        
        // Wait for the benchmark to finish, indicated by the task generator shutting down
        ServiceManagerUtils.awaitTerminatedOrStopAfterTimeout(taskGeneratorService, 5, TimeUnit.MINUTES, 60, TimeUnit.SECONDS);
        
        
        logger.info("Starting evaluation module... ");
        evaluationModuleService.startAsync();
        // TODO If we do await running, it seems it blocks forever as terminated or failure is not handled properly
        evaluationModuleService.awaitTerminated(60, TimeUnit.SECONDS);

        // Wait for the result


        // TODO: The better solution would be to listen on the taskAck channel to see whether there is any activity ongoing
        
        logger.debug("Awaiting evaluation result...");
        //evaluationDataReceivedFuture.get(60, TimeUnit.SECONDS);
        evaluationDataReceivedFuture.get(10, TimeUnit.MINUTES);

        
        logger.info("Benchmark controller done.");

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
    public void shutDown() throws Exception {
        ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, 60, TimeUnit.SECONDS);
    }

}



