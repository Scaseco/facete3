package org.hobbit.benchmarks.faceted_benchmark.main;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.core.SparqlService;

/**
 * Tag interface for dependency injection
 *
 * @author Claus Stadler 2017-09-16
 *
 */
public interface SparqlServiceSupplier
    extends Supplier<SparqlService>
{

}
