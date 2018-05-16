package org.hobbit.core.component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.ResourceUtils;
import org.hobbit.core.Commands;
import org.hobbit.core.Constants;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.service.api.ServiceBuilder;
import org.hobbit.core.utils.ByteChannelUtils;
import org.hobbit.core.utils.PublisherUtils;
import org.hobbit.core.utils.ServiceManagerUtils;
import org.hobbit.vocab.HOBBIT;
import org.hobbit.vocab.HobbitErrors;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.primitives.Bytes;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;

@Component
@Qualifier("MainService")
public class BenchmarkControllerFacetedBrowsing
    extends ComponentBaseExecutionThread
    //implements BenchmarkController
{
    public static final int MAX_DATAGENERATION_TIME_IN_SECONDS = 60 * 15;
    
    // Time allowed for the task generator to finish
    public static final int MAX_BENCHMARK_TIME_IN_SECONDS = 60 * 45;    

    public static final int MAX_COMPONENT_STARTUP_TIME_IN_SECONDS = 60 * 3;    
    public static final int MAX_COMPONENT_SHUTDOWN_TIME_IN_SECONDS = 60;
 
    // Short requests should usually be served within a few seconds
    // Example is the request to retrieve data from the ES
    // TODO We could break these different times down into separate constants
    public static final int MAX_SHORT_REQUEST_TIME_IN_SECONDS = 15;

    // Long requests are e.g. starting a docker container
    public static final int MAX_LONG_REQUEST_TIME_IN_SECONDS = 60;

    // Long requests are e.g. starting a docker container
    public static final int MAX_TASK_EXECUTION_TIME_IN_SECONDS = 60 * 5;

    
    public static final int MAX_EVAL_ANALYSIS_TIME_IN_SECONDS = 60 * 5;
    
    //  
    
    
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



    @Resource(name="commandSender")
    protected Subscriber<ByteBuffer> commandSender;

    @Resource(name="experimentResult")
    protected org.apache.jena.rdf.model.Resource experimentResult;
    
    // Service manager that upon initialization holds services for the DG, TG and ES
    protected ServiceManager serviceManager;


//    @Resource
//    protected AbstractEvaluationStorage HACKmockEs;

    //protected CompletableFuture<ByteBuffer> systemUnderTestReadyFuture;

    // FIXME HACK - A service should not be considered started unless they are ready
    // So these futures should actually be managed with the service abstraction
    protected CompletableFuture<ByteBuffer> systemReadyFuture;
    protected CompletableFuture<ByteBuffer> dataGeneratorReadyFuture;
    protected CompletableFuture<ByteBuffer> taskGeneratorReadyFuture;
    protected CompletableFuture<ByteBuffer> evalStorageReadyFuture;
    
    
    //protected CompletableFuture<ByteBuffer> evalModuleReadyFuture;

    // The future for whether the evaluation data has been received
    protected CompletableFuture<ByteBuffer> evaluationDataReceivedFuture;


    protected Service dataGeneratorService;
    protected Service taskGeneratorService;
    protected Service systemAdapterService;

    protected Service evaluationStorageService;
    protected Service evaluationModuleService;


    protected CompletableFuture<State> dataGenerationTerminatedFuture;
    protected CompletableFuture<State> taskGenerationTerminatedFuture;

    protected CompletableFuture<?> initFuture;

    //public static final byte START_BENCHMARK_SIGNAL = 66;

    // https://stackoverflow.com/questions/30025428/listfuture-to-futurelist-sequence
    @SafeVarargs
	public static <T> CompletableFuture<List<T>> allAsList(CompletableFuture<T> ... futures) {
    	return allAsList(Arrays.asList(futures));
    }
    
    public static <T> CompletableFuture<List<T>> allAsList(List<CompletableFuture<T>> futures) {
        return CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[futures.size()])
        ).thenApply(ignored ->
            futures.stream().map(CompletableFuture::join).collect(Collectors.toList())
        );
    }

    @Override
    public void startUp() {
        logger.info("BenchmarkController::startUp()");
    	super.startUp();
    	

        // The system adapter will send a ready signal, hence register on it on the command queue before starting the service
        // NOTE A completable future will resolve only once; Java 9 flows would allow multiple resolution (reactive streams)
//        systemUnderTestReadyFuture = PublisherUtils.triggerOnMessage(commandPublisher,
//                ByteChannelUtils.firstByteEquals(Commands.SYSTEM_READY_SIGNAL));

        dataGeneratorReadyFuture = PublisherUtils.triggerOnMessage(commandReceiver,
                ByteChannelUtils.firstByteEquals(Commands.DATA_GENERATOR_READY_SIGNAL));

        
  
//        dataGeneratorBulkLoadReadyFuture = PublisherUtils.triggerOnMessage(commandReceiver,
//                ByteChannelUtils.firstByteEquals(MochaConstants.BULK_LOAD_FROM_DATAGENERATOR));
        
        // Sum up results of the finished bulk loading events
        CompletableFuture<Entry<Long, Long>> dgBulkLoadingFinishedFutures = allAsList(
        		PublisherUtils.triggerOnMessage(commandReceiver,
                        ByteChannelUtils.firstByteEquals(MochaConstants.BULK_LOAD_FROM_DATAGENERATOR))
        ).thenApply(msgs -> {
        	Entry<Long, Long> r = msgs.stream()
        			.map(buffer -> {
        				buffer = buffer.duplicate();
        				buffer.get(); // discard first byte
        				Entry<Long, Long> s = new SimpleEntry<>(buffer.getLong(), buffer.getLong());
        				return s;
        			})
        			.reduce(new SimpleEntry<>(0l, 0l), (x, y) -> new SimpleEntry<>(x.getKey() + y.getKey(), x.getValue() + y.getValue()));
        	return r;
        });

        dgBulkLoadingFinishedFutures.whenComplete((numRecordsAndBatches, t) -> {
        	
        	logger.info("Bulk loading finished - total records and batches: " + numRecordsAndBatches);
        	
        	// Send out the message that data generation completed
            boolean isLastBulkLoad = true;
            int numBatches = numRecordsAndBatches.getValue().intValue();
        	commandSender.onNext((ByteBuffer)ByteBuffer.allocate(6)
        			.put(MochaConstants.BULK_LOAD_DATA_GEN_FINISHED)
        			.putInt(numBatches)
        			.put((byte)(isLastBulkLoad ? 1 : 0))
        			.rewind());

        });
        
//        dataGeneratorBulkLoadDoneFuture = PublisherUtils.triggerOnMessage(commandReceiver,
//                ByteChannelUtils.firstByteEquals(MochaConstants.));
        
        // TODO Enable waiting for BULK_LOADING_DATA_FINISHED
        
        
        taskGeneratorReadyFuture = PublisherUtils.triggerOnMessage(commandReceiver,
                ByteChannelUtils.firstByteEquals(Commands.TASK_GENERATOR_READY_SIGNAL));

        evalStorageReadyFuture = PublisherUtils.triggerOnMessage(commandReceiver,
                ByteChannelUtils.firstByteEquals(Commands.EVAL_STORAGE_READY_SIGNAL));

//        evalModuleReadyFuture = PublisherUtils.triggerOnMessage(commandReceiver,
//                ByteChannelUtils.firstByteEquals(Commands.EVAL_MODULE_READY_SIGNAL));
        
        dataGeneratorReadyFuture.whenComplete((v, t) -> { logger.info("DataGenerator ready signal received"); });
        taskGeneratorReadyFuture.whenComplete((v, t) -> { logger.info("TaskGenerator ready signal received"); });
        evalStorageReadyFuture.whenComplete((v, t) -> { logger.info("EvalStorage ready signal received"); });
        //evalModuleReadyFuture.whenComplete((v, t) -> { logger.info("EvalModule ready signal received"); });


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
        
        dataGenerationTerminatedFuture.whenComplete((v, t) -> {
        	logger.info("BC got DG termination notification - " + v + " - " + t);
        });
        
        
        taskGenerationTerminatedFuture =
        		ServiceManagerUtils.awaitState(taskGeneratorService, State.TERMINATED)
        		.whenComplete((v, t) -> {
//            try {
                logger.info("Sending out task TASK_GENERATION_FINISHED signal");
                commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATION_FINISHED}));
//            } catch(IOException e) {
//                throw new RuntimeException(e);
//            }
        		});

        
        evaluationDataReceivedFuture =
    		PublisherUtils.triggerOnMessage(commandReceiver, ByteChannelUtils.firstByteEquals(Commands.EVAL_MODULE_FINISHED_SIGNAL))
    		.whenComplete((buffer, ex) -> {
	            logger.info("Evaluation model received");
	            Model evalModel = RabbitMQUtils.readModel(buffer.array(), 1, buffer.limit() - 1);
//	            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//	            RDFDataMgr.write(baos, evalModel, Lang.NTRIPLES);
//	            String str = baos.toString();
	            //logger.info("Received eval model is: " + str);
	            
	            // In evalModel, rename any NEW_EXPERIMENT_URI resources to the experiment URI
	            org.apache.jena.rdf.model.Resource tmp = evalModel.createResource(Constants.NEW_EXPERIMENT_URI);
	            ResourceUtils.renameResource(tmp, experimentResult.getURI());
	            
	            // Then add them to the final result model
	            experimentResult.getModel().add(evalModel);
    		});
        
        serviceManager = new ServiceManager(Arrays.asList(
                dataGeneratorService,
                taskGeneratorService,
                evaluationStorageService
        ));
        

//        boolean hack = false;
//        if(hack) {
//	        try {
//				HACKmockEs.init();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        }
        
        //evaluationModuleService
        //systemAdapterService,

        systemReadyFuture = PublisherUtils.triggerOnMessage(commandReceiver,
                ByteChannelUtils.firstByteEquals(Commands.START_BENCHMARK_SIGNAL))
        	.whenComplete((b, e) -> {
        		if(e == null) {
		        	b = b.duplicate();
		        	b.get(); // Skip first byte
		        	
		        	byte[] bs = new byte[b.remaining()];
		        	b.get(bs);
		            String systemContainerId = new String(bs, StandardCharsets.UTF_8);
		            logger.info("System container id: " + systemContainerId);
        		}
	        	// Register a 
        	});
//        	.whenComplete((x, e) -> {
//        		if(e == null) {
//		            logger.info("BenchmarkController::startUp() Waiting for services to start...");
//		            ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(
//		                    serviceManager,
//		                    60, TimeUnit.SECONDS,
//		                    60, TimeUnit.SECONDS);
//		
//		            logger.info("BenchmarkController::startUp() completed");
//        		}
//        	});

        
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

        logger.info("BenchmarkController::startUp() Waiting for services to start...");
        ServiceManagerUtils.startAsyncAndAwaitHealthyAndStopOnFailure(
                serviceManager,
                MAX_COMPONENT_STARTUP_TIME_IN_SECONDS, TimeUnit.SECONDS,
                MAX_COMPONENT_STARTUP_TIME_IN_SECONDS, TimeUnit.SECONDS);


        logger.info("Waiting for system, data and task generators to become ready");
        //systemReadyFuture
        initFuture = CompletableFuture.allOf(dataGeneratorReadyFuture, taskGeneratorReadyFuture, evalStorageReadyFuture);

//        initFuture.whenComplete((v, t) -> {
//        	run();
//        });
        //initFuture.get(60, TimeUnit.SECONDS);

        logger.info("BenchmarkController::startUp() completed");
    }

    @Override
    public void run() throws Exception {
    	try {
    		runCore();
    	} catch(Exception e) {
    		experimentResult
    			.addProperty(HOBBIT.terminatedWithError, HobbitErrors.BenchmarkCrashed);
    		
    		throw new RuntimeException(e);
        } finally {
	    	
	    	logger.info("Result Model: " + RabbitMQUtils.writeModel2String(experimentResult.getModel()));
	    	
	        commandSender.onNext(ByteBuffer.wrap(Bytes.concat(
	        		new byte[] {Commands.BENCHMARK_FINISHED_SIGNAL},
	        		RabbitMQUtils.writeModel(experimentResult.getModel()))));
        }
    }

    public void runCore() throws InterruptedException, ExecutionException, TimeoutException {

        initFuture.get(MAX_COMPONENT_STARTUP_TIME_IN_SECONDS, TimeUnit.SECONDS);
        
        
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


        logger.info("Waiting for data generation phase to complete");
        CompletableFuture<?> dataGenerationPhaseCompletion = CompletableFuture.allOf(
                dataGenerationTerminatedFuture);
                //systemUnderTestReadyFuture);

        try {
            dataGenerationPhaseCompletion.get(MAX_DATAGENERATION_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch(Exception e) {
            throw new RuntimeException("Data generation phase failed or did not complete in time", e);
        }

// TODO Send out MochaConstants.BULK_LOAD_DATA_GEN_FINISHED [4bytes numSentMessages]
//        boolean lastBulkLoad = true;
//        ByteBuffer buffer = ByteBuffer.allocate(5);
//        buffer.putInt(this.numberOfDataGenerators);
//        buffer.put(lastBulkLoad ? (byte) 1 : (byte) 0);
//sendToCmdQueue(VirtuosoSystemAdapterConstants.BULK_LOAD_DATA_GEN_FINISHED, buffer.array());

//        boolean isLastBulkLoad = true;
//        int numDataGenerators = 1;
//    	commandSender.onNext((ByteBuffer)ByteBuffer.allocate(6)
//    			.put(MochaConstants.BULK_LOAD_DATA_GEN_FINISHED)
//    			.putInt(numDataGenerators)
//    			.put((byte)(isLastBulkLoad ? 1 : 0))
//    			.rewind());
        
        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.DATA_GENERATION_FINISHED}));

        logger.info("Sending TASK_GENERATOR_START_SIGNAL");
        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.TASK_GENERATOR_START_SIGNAL}));



        logger.info("Waiting for task generation phase to complete");
        CompletableFuture<?> taskGenerationPhaseCompletion = taskGenerationTerminatedFuture;
                //CompletableFuture.allOf(dataGenerationFuture);

        try {
            taskGenerationPhaseCompletion.get(MAX_BENCHMARK_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } catch(Exception e) {
            throw new RuntimeException("Task generation and benchmarking phase failed or did not complete in time", e);
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
        try {
			evaluationModuleService.awaitTerminated(MAX_EVAL_ANALYSIS_TIME_IN_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}

        // Wait for the result


        // TODO: The better solution would be to listen on the taskAck channel to see whether there is any activity ongoing
        
        logger.info("Awaiting evaluation result...");
        //evaluationDataReceivedFuture.get(60, TimeUnit.SECONDS);
        try {
			evaluationDataReceivedFuture.get(MAX_COMPONENT_STARTUP_TIME_IN_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			throw new RuntimeException(e);
		}

        
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
    protected void triggerShutdown() {
        logger.info("BenchmarkController::triggerShutdown() invoked");
    	initFuture.cancel(true);
    	dataGenerationTerminatedFuture.cancel(true);
    	taskGenerationTerminatedFuture.cancel(true);
    	evaluationDataReceivedFuture.cancel(true);
    	
    	super.triggerShutdown();
        logger.info("BenchmarkController::triggerShutdown() invoked");
    }
    
    @Override
    public void shutDown() {
        logger.info("BenchmarkController::shutDown() invoked");
    	
    	try {
        	ServiceManagerUtils.stopAsyncAndWaitStopped(serviceManager, MAX_COMPONENT_SHUTDOWN_TIME_IN_SECONDS, TimeUnit.SECONDS);
        } finally {
        	super.shutDown();
        }
        logger.info("BenchmarkController::shutDown() completed");
    }

}



