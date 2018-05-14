package org.hobbit.core.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import javax.annotation.Resource;
import javax.inject.Inject;

import org.apache.jena.ext.com.google.common.primitives.Bytes;
import org.apache.jena.rdf.model.Model;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractEvaluationStorage;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import io.reactivex.disposables.Disposable;

@Component
@Qualifier("MainService")
public class EvaluationModuleComponent
    extends ComponentBaseExecutionThread
{
    private static final Logger logger = LoggerFactory.getLogger(EvaluationModuleComponent.class);

    @Resource(name="commandSender")
    protected Subscriber<ByteBuffer> commandSender;

//    @Resource(name="es2emReceiver")
//    protected Flowable<ByteBuffer> fromEvaluationStorage;
//
//    @Resource(name="em2esSender")
//    protected Subscriber<ByteBuffer> toEvaluationStorage;

    @Resource(name="em2esClient")
    Function<ByteBuffer, CompletableFuture<ByteBuffer>> em2esClient;
    
    //@Resource(name="evaluationModule")
    @Inject
    protected EvaluationModule evaluationModule;
    
    protected byte requestBody[];

    
    protected Disposable esSubscription;
    
    
    protected CompletableFuture<?> terminationFuture = new CompletableFuture<Object>();
    
    @Override
    public void startUp() {
    	logger.info("EvalModule::startUp() invoked");
    	super.startUp();
        //collectResponses();


        // TODO Not sure if this properly emulates the protocol to the evaluation storage

        //EvaluationModuleFacetedBrowsingBenchmark evaluationCore = new EvaluationModuleFacetedBrowsingBenchmark();
        try {
			evaluationModule.init();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}

//        boolean terminationConditionSatisfied[] = {false};
        
//        esSubscription = fromEvaluationStorage.subscribe(buffer -> {
        
        //run();
    }


    public void collectResponses() throws InterruptedException, ExecutionException, TimeoutException {
        requestBody = new byte[] { AbstractEvaluationStorage.NEW_ITERATOR_ID };

        logger.info("EM now requesting data from ES");
        while (true) {
        	CompletableFuture<ByteBuffer> responseFuture = em2esClient.apply(ByteBuffer.wrap(requestBody));
        	ByteBuffer buffer = responseFuture.get(BenchmarkControllerFacetedBrowsing.MAX_SHORT_REQUEST_TIME_IN_SECONDS, TimeUnit.SECONDS);
        
            if (buffer.remaining() == 0) {
                throw new IllegalStateException("Protocol error: Got a completely empty response from the evaluation storage.");
            }
            requestBody[0] = buffer.get();

            // if the response is empty
            if (buffer.remaining() == 0) {
                break;
            }
            
            byte[] expData = RabbitMQUtils.readByteArray(buffer);
            long taskSentTimestamp = expData.length > 0 ? RabbitMQUtils.readLong(expData) : 0;
            byte[] expectedData = RabbitMQUtils.readByteArray(buffer);

            byte[] actData = RabbitMQUtils.readByteArray(buffer);
            long responseReceivedTimestamp = actData.length > 0 ? RabbitMQUtils.readLong(actData) : 0;
            byte[] receivedData = RabbitMQUtils.readByteArray(buffer);

            try {
            	evaluationModule.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    
    private void sendResultModel(Model model) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        model.write(outputStream, "JSONLD");
        commandSender.onNext(ByteBuffer.wrap(Bytes.concat(new byte[] {Commands.EVAL_MODULE_FINISHED_SIGNAL}, outputStream.toByteArray())));
    }

    @Override
    public void run() throws Exception {
    	collectResponses();
        
        commandSender.onNext(ByteBuffer.wrap(new byte[]{Commands.EVAL_MODULE_READY_SIGNAL}));
        Model model = evaluationModule.summarizeEvaluation();
        logger.info("The result model has " + model.size() + " triples.");
        sendResultModel(model);
      

    }

    @Override
    public void triggerShutdown() {
    	esSubscription.dispose();

    	super.triggerShutdown();
        // TODO Auto-generated method stub

    }

}


//ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//model.write(outputStream, "JSONLD");
//ByteBuffer buf = ByteBuffer.allocate(1 + outputStream.size());
//buf.put(Commands.EVAL_MODULE_FINISHED_SIGNAL);
//buf.put(outputStream.toByteArray());
//buf.rewind();
////try {
//  commandSender.onNext(buf);

//try {
    //toEvaluationStorage.onNext(ByteBuffer.wrap(requestBody));
//} catch (IOException e1) {
//    throw new RuntimeException(e1);
//}

//logger.info("Running evaluation module");
//// wait for having served 1 request
//
//terminationFuture.get(60, TimeUnit.SECONDS);
//
//logger.info("Evaluation module terminating");
//public ByteBuffer processMessage(ByteBuffer buffer) {
//	buffer = buffer.duplicate();
//
//	if(terminationConditionSatisfied[0]) {
//		throw new RuntimeException("Got another message after termination message: " + Arrays.toString(buffer.array()));
//	}
//
//    logger.info("Received data to evaluate");
//
//    // if the response is empty
//    if (!buffer.hasRemaining()) {
//        logger.error("Got a completely empty response from the evaluation storage.");
//        return;
//    }
//
//    requestBody[0] = buffer.get();
//
//    // if the response is empty
//    if (!buffer.hasRemaining()) {
//        // This is the 'finish' condition
//    	terminationConditionSatisfied[0] = true;
//    	
//        Model model = evaluationModule.summarizeEvaluation();
//        logger.info("The result model has " + model.size() + " triples.");
//
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        model.write(outputStream, "JSONLD");
//        ByteBuffer buf = ByteBuffer.allocate(1 + outputStream.size());
//        buf.put(Commands.EVAL_MODULE_FINISHED_SIGNAL);
//        buf.put(outputStream.toByteArray());
//        buf.rewind();
//        try {
//            commandSender.onNext(buf);
//        } catch(Exception e) {
//            throw new RuntimeException(e);
//        } finally {
//        	terminationFuture.complete(null);
//        }
//        
//
//    } else {
//        // If we did not encounter the end condition, request more data
//        byte[] data = RabbitMQUtils.readByteArray(buffer);
//        long taskSentTimestamp = data.length > 0 ? RabbitMQUtils.readLong(data) : 0;
//        byte[] expectedData = RabbitMQUtils.readByteArray(buffer);
//
//        data = RabbitMQUtils.readByteArray(buffer);
//        long responseReceivedTimestamp = data.length > 0 ? RabbitMQUtils.readLong(data) : 0;
//        byte[] receivedData = RabbitMQUtils.readByteArray(buffer);
//
//        try {
//        	evaluationModule.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        
////        try {
//            toEvaluationStorage.onNext(ByteBuffer.wrap(requestBody));
////        } catch (IOException e1) {
////            throw new RuntimeException(e1);
////        }                
//    }
//}

