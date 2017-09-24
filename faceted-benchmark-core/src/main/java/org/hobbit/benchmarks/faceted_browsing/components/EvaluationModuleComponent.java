package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import javax.annotation.Resource;

import org.apache.jena.rdf.model.Model;
import org.hobbit.core.Commands;
import org.hobbit.core.components.AbstractEvaluationStorage;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.evaluation.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.transfer.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EvaluationModuleComponent
    extends ComponentBase
{
    private static final Logger logger = LoggerFactory.getLogger(EvaluationModuleComponent.class);

    @Resource(name="commandChannel")
    protected WritableByteChannel commandChannel;

    @Resource(name="es2emPub")
    protected Publisher<ByteBuffer> fromEvaluationStorage;

    @Resource(name="em2es")
    protected WritableByteChannel toEvaluationStorage;


    @Override
    public void init() throws Exception {

        commandChannel.write(ByteBuffer.wrap(new byte[]{Commands.EVAL_MODULE_READY_SIGNAL}));
        //collectResponses();


        // TODO Not sure if this properly emulates the protocol to the evaluation storage

        byte requestBody[] = new byte[] { AbstractEvaluationStorage.NEW_ITERATOR_ID };


        EvaluationModuleFacetedBrowsingBenchmark evaluationCore = new EvaluationModuleFacetedBrowsingBenchmark();

        fromEvaluationStorage.subscribe(buffer -> {


            logger.debug("Received data to evaluate");

            try {
                toEvaluationStorage.write(ByteBuffer.wrap(requestBody));
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }

            // if the response is empty
            if (!buffer.hasRemaining()) {
                logger.error("Got a completely empty response from the evaluation storage.");
                return;
            }

            requestBody[0] = buffer.get();

            // if the response is empty
            if (!buffer.hasRemaining()) {
                // This is the 'finish' condition
                Model model = evaluationCore.summarizeEvaluation();
                logger.info("The result model has " + model.size() + " triples.");

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                model.write(outputStream, "JSONLD");
                ByteBuffer buf = ByteBuffer.allocate(1 + outputStream.size());
                buf.put(Commands.EVAL_MODULE_FINISHED_SIGNAL);
                buf.put(outputStream.toByteArray());
                try {
                    commandChannel.write(buf);
                } catch(Exception e) {
                    throw new RuntimeException(e);
                }


                return;
            }


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
        });


        try {
            toEvaluationStorage.write(ByteBuffer.wrap(requestBody));
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }

    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

}
