package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Collection;

import org.apache.jena.sparql.core.Quad;
import org.apache.jena.update.UpdateRequest;

/**
 * Interface for creating insert requests for a given collection of quads.
 * 'Simple' denotes that only a single request as created for a collection, which
 * contrasts approaches that yield multiple requests.
 * 
 * @author raven May 12, 2018
 *
 */
public interface SimpleSparqlInsertRequestFactory {
	UpdateRequest createUpdateRequest(Collection<Quad> insertQuads);
}
