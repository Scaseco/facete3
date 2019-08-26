package org.hobbit.benchmark.faceted_browsing.v1.config;

import java.nio.ByteBuffer;
import java.util.Set;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.component.QueryID;
import org.hobbit.benchmark.faceted_browsing.v1.evaluation.ChokePoints;
import org.hobbit.core.component.BenchmarkVocab;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetedBrowsingEncodersV1 {
	
	private static final Logger logger = LoggerFactory.getLogger(FacetedBrowsingEncodersV1.class);

	
    // v1 format: [taskId: String] [scenarioId: String] [query: String] [ResultSet: String/Json]
    public static Resource decodeExpectedDataV1(ByteBuffer bufferExp) {
        String taskidGold = RabbitMQUtils.readString(bufferExp);
        logger.info("Eval_mod task Id: "+ taskidGold);
        String scenarioStr = RabbitMQUtils.readString(bufferExp);
        logger.info("Scenario id: "+ scenarioStr);
        String queryIdStr = RabbitMQUtils.readString(bufferExp);
        logger.info("query: "+ queryIdStr); // This is just the query id - not the string

        String goldsString = RabbitMQUtils.readString(bufferExp);
//         LOGGER.info("goldsString: "+ goldsString);

        int scenarioId = Integer.parseInt(scenarioStr);
        int queryId = Integer.parseInt(queryIdStr);
        
        Resource result = ModelFactory.createDefaultModel().createResource(taskidGold)
        		.addLiteral(FacetedBrowsingVocab.scenarioId, scenarioId)
        		.addLiteral(FacetedBrowsingVocab.queryId, queryId)
        		.addLiteral(BenchmarkVocab.expectedResult, goldsString);

        QueryID key = new QueryID(scenarioId, queryId);

        if(scenarioId != 0) {
	        Set<Integer> cps = ChokePoints.getChokpointsForQueryId(key);
	        if(cps == null) {
	        	throw new RuntimeException("No chokepoint for query id: " + key);
	        }

	        for(Integer cp : cps) {
	        	result.addLiteral(FacetedBrowsingVocab.transitionType, cp);
	        }
        }

        return result;
    }
}
