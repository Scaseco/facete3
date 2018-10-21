package org.hobbit.benchmark.faceted_browsing.v2.impl;

import org.apache.jena.rdf.model.Model;
import org.hobbit.core.component.EvaluationModule;

/**
 * Generic RDF-based evaluation module (based on vocabulary)
 * 
 * @author Claus Stadler, Oct 19, 2018
 *
 */
public class EvaluationModuleRdfGeneric
	implements EvaluationModule
{
	@Override
	public void init() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp,
			long responseReceivedTimestamp) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Model summarizeEvaluation() {
		// TODO Auto-generated method stub
		return null;
	}

}
