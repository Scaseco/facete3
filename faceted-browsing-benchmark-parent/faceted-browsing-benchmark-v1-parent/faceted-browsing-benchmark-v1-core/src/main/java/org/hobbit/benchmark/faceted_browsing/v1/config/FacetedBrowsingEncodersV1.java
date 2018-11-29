package org.hobbit.benchmark.faceted_browsing.v1.config;

import java.nio.ByteBuffer;
import java.util.Set;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.hobbit.benchmark.faceted_browsing.component.FacetedBrowsingVocab;
import org.hobbit.benchmark.faceted_browsing.component.QueryID;
import org.hobbit.benchmark.faceted_browsing.v1.evaluation.ChokePoints;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FacetedBrowsingEncodersV1 {
	
	private static final Logger logger = LoggerFactory.getLogger(FacetedBrowsingEncodersV1.class);

	
    // v1 format: [taskId: String] [scenarioId: String] [query: String] [ResultSet: String/Json]
    public static Resource decodeExpectedDataV1(ByteBuffer bufferExp) {
        String taskidGold = RabbitMQUtils.readString(bufferExp);
        logger.info("Eval_mod task Id: "+ taskidGold);
        String scenario = RabbitMQUtils.readString(bufferExp);
        logger.info("Scenario id: "+ scenario);
        String query = RabbitMQUtils.readString(bufferExp);
        logger.info("query: "+ query); // I think this is just the query id - not the string
        
        Resource result = ModelFactory.createDefaultModel().createResource(taskidGold)
        		.addLiteral(FacetedBrowsingVocab.scenarioId, scenario)
        		.addLiteral(FacetedBrowsingVocab.queryId, query);

        QueryID key = new QueryID(Integer.parseInt(scenario), Integer.parseInt(query));

        Set<Integer> cps = ChokePoints.getChokpointsForQueryId(key);

        for(Integer cp : cps) {
        	result.addLiteral(FacetedBrowsingVocab.chokepointId, cp);
        }

        return result;
    }
}
