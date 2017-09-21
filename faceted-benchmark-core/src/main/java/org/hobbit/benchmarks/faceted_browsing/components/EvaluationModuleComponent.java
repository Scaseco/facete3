package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.annotation.Resource;

import org.hobbit.evaluation.EvaluationModuleFacetedBrowsingBenchmark;
import org.hobbit.transfer.Publisher;
import org.springframework.stereotype.Component;

@Component
public class EvaluationModuleComponent
    extends ComponentBase
{
    @Resource(name="sa2esPub")
    protected Publisher<ByteBuffer> fromEvaluationStorage;

    @Override
    public void init() throws Exception {
        // TODO Auto-generated method stub

        EvaluationModuleFacetedBrowsingBenchmark evaluationCore = new EvaluationModuleFacetedBrowsingBenchmark();

        fromEvaluationStorage.subscribe(buffer -> {


            System.out.println("Received data to evaluate");



            //evaluationCore.evaluateResponse(expectedData, receivedData, taskSentTimestamp, responseReceivedTimestamp);
            //evaluationCore.
        });


    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

}
