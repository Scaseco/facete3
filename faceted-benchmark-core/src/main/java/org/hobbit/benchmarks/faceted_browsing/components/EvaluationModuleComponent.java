package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

import javax.annotation.Resource;

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


    @Resource(name="es2emPub")
    protected Publisher<ByteBuffer> fromEvaluationStorage;

    @Resource(name="em2es")
    protected WritableByteChannel toEvaluationStorage;


    @Override
    public void init() throws Exception {

        // TODO Not sure if this properly emulatess the protocol to the evaluation storage

        byte requestBody[] = new byte[] { AbstractEvaluationStorage.NEW_ITERATOR_ID };
        try {
            toEvaluationStorage.write(ByteBuffer.wrap(requestBody));
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }



        EvaluationModuleFacetedBrowsingBenchmark evaluationCore = new EvaluationModuleFacetedBrowsingBenchmark();

        fromEvaluationStorage.subscribe(buffer -> {


            System.out.println("Received data to evaluate");

            try {
                toEvaluationStorage.write(ByteBuffer.wrap(requestBody));
            } catch (IOException e1) {
                throw new RuntimeException(e1);
            }

            // if the response is empty
            if (buffer.remaining() == 0) {
                logger.error("Got a completely empty response from the evaluation storage.");
                return;
            }

            requestBody[0] = buffer.get();

            // if the response is empty
            if (buffer.remaining() == 0) {
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


    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

}
