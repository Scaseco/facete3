package org.hobbit.core.component;

import org.apache.jena.rdf.model.Model;

public interface EvaluationModule {
	void init() throws Exception;
	
    void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp,
            long responseReceivedTimestamp) throws Exception;

    Model summarizeEvaluation();
}
