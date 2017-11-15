package org.hobbit.core.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.Resource;

import org.apache.jena.rdf.model.Model;
import org.hobbit.benchmark.faceted_browsing.evaluation.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractEvaluationStorage;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.core.service.api.RunnableServiceCapable;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.reactivex.Flowable;

@Component
public class EvaluationModuleComponent
    extends ComponentBase
    implements RunnableServiceCapable
{
    private static final Logger logger = LoggerFactory.getLogger(EvaluationModuleComponent.class);

    @Resource(name="commandChannel")
    protected Subscriber<ByteBuffer> commandChannel;

    @Resource(name="es2emPub")
    protected Flowable<ByteBuffer> fromEvaluationStorage;

    @Resource(name="em2es")
    protected Subscriber<ByteBuffer> toEvaluationStorage;

    
    protected byte requestBody[];

    @Override
    public void startUp() throws Exception {
        //collectResponses();


        // TODO Not sure if this properly emulates the protocol to the evaluation storage

        requestBody = new byte[] { AbstractEvaluationStorage.NEW_ITERATOR_ID };


        EvaluationModuleFacetedBrowsingBenchmark evaluationCore = new EvaluationModuleFacetedBrowsingBenchmark();
        evaluationCore.init();

        boolean terminationConditionSatisfied[] = {false};
        
        fromEvaluationStorage.subscribe(buffer -> {

        	if(terminationConditionSatisfied[0]) {
        		throw new RuntimeException("Got another message after termination message");
        	}

            logger.debug("Received data to evaluate");

            // if the response is empty
            if (!buffer.hasRemaining()) {
                logger.error("Got a completely empty response from the evaluation storage.");
                return;
            }

            requestBody[0] = buffer.get();

            // if the response is empty
            if (!buffer.hasRemaining()) {
                // This is the 'finish' condition
            	terminationConditionSatisfied[0] = true;
            	
                Model model = evaluationCore.summarizeEvaluation();
                logger.info("The result model has " + model.size() + " triples.");

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                model.write(outputStream, "JSONLD");
                ByteBuffer buf = ByteBuffer.allocate(1 + outputStream.size());
                buf.put(Commands.EVAL_MODULE_FINISHED_SIGNAL);
                buf.put(outputStream.toByteArray());
                try {
                    commandChannel.onNext(buf);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }


            } else {
                // If we did not encounter the end condition, request more data
                byte[] data = RabbitMQUtils.readByteArray(buffer);
                long taskSentTimestamp = data.length > 0 ? RabbitMQUtils.readLong(data) : 0;
                byte[] expectedData = RabbitMQUtils.readByteArray(buffer);

                data = RabbitMQUtils.readByteArray(buffer);
                long responseReceivedTimestamp = data.length > 0 ? RabbitMQUtils.readLong(data) : 0;
                byte[] receivedData = RabbitMQUtils.readByteArray(buffer);

                try {
                    evaluationCore.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                
//                try {
                    toEvaluationStorage.onNext(ByteBuffer.wrap(requestBody));
//                } catch (IOException e1) {
//                    throw new RuntimeException(e1);
//                }                
            }

        });

        commandChannel.onNext(ByteBuffer.wrap(new byte[]{Commands.EVAL_MODULE_READY_SIGNAL}));
    }


    @Override
    public void run() throws Exception {
//        try {
            toEvaluationStorage.onNext(ByteBuffer.wrap(requestBody));
//        } catch (IOException e1) {
//            throw new RuntimeException(e1);
//        }
        
        logger.debug("Running evaluation module");
    }

    @Override
    public void shutDown() throws IOException {
        // TODO Auto-generated method stub

    }

}
