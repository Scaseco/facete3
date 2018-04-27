package org.hobbit.core.service.api;

import java.util.function.Supplier;

import org.aksw.jena_sparql_api.core.service.SparqlBasedService;
import org.apache.jena.rdfconnection.RDFConnection;
import org.hobbit.core.service.docker.DockerService;

public abstract class SparqlDockerApiService
	extends DockerApiService<DockerService, Supplier<RDFConnection>>
	implements SparqlBasedService
{
	public SparqlDockerApiService(DockerService delegate) {
		super(delegate);
	}
	
	@Override
	public RDFConnection createDefaultConnection() {
		RDFConnection result = getApi().get();
		return result;
	}
}
