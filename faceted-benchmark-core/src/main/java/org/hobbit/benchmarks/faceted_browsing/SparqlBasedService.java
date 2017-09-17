package org.hobbit.benchmarks.faceted_browsing;

import org.apache.jena.rdfconnection.RDFConnection;

import com.google.common.util.concurrent.Service;

public interface SparqlBasedService
    extends Service
{
    RDFConnection createDefaultConnection();
}
