package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.Collection;
import java.util.Collections;

import org.aksw.jena_sparql_api.core.utils.UpdateRequestUtils;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.update.UpdateRequest;

import com.google.common.collect.EvictingQueue;

/**
 * A sparql insert request builder that in addition to creating {@link UpdateRequest}
 * instances, also adds the quads to a queue. As soon as this queue runs full, all
 * subsequent created update request will include removal of the
 * quads of the oldest entry in the queue.
 * 
 * and removes a
 * 
 * @author raven May 12, 2018
 *
 */
public class SimpleSparqlInsertRequestFactoryWindowedInMemory
	implements SimpleSparqlInsertRequestFactory
{
	protected EvictingQueue<Collection<Quad>> evictingQueue;
	
	public SimpleSparqlInsertRequestFactoryWindowedInMemory(int maxSize) {
		this(EvictingQueue.create(maxSize));
	}
	
	public SimpleSparqlInsertRequestFactoryWindowedInMemory(EvictingQueue<Collection<Quad>> evictingQueue) {
		this.evictingQueue = evictingQueue;
	}
	
	@Override
	public synchronized UpdateRequest createUpdateRequest(Collection<Quad> insertQuads) {
		int remainingCapacity = evictingQueue.remainingCapacity();

		Collection<Quad> removalQuads = remainingCapacity == 0
				? evictingQueue.remove()
				: Collections.emptyList();

		evictingQueue.add(insertQuads);
		UpdateRequest result = UpdateRequestUtils.createUpdateRequest(insertQuads, removalQuads);
		return result;
	}
}