package org.hobbit.benchmarks.faceted_benchmark.main;

import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.apache.jena.rdf.model.ModelFactory;

/**
 * A simple SparqlServiceSupplier implementation.
 *
 * @author raven
 *
 */
public class SparqlServiceSupplierDefault
    implements SparqlServiceSupplier
{
    @Override
    public SparqlService get() {
        SparqlService result = FluentSparqlService.from(ModelFactory.createDefaultModel()).create();
        return result;
    }
}
