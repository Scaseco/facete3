package org.hobbit.benchmarks.faceted_benchmark.main;

import org.aksw.jena_sparql_api.core.FluentQueryExecutionFactory;
import org.aksw.jena_sparql_api.core.SparqlService;
import org.aksw.jena_sparql_api.update.FluentSparqlService;
import org.aksw.jena_sparql_api.update.FluentUpdateExecutionFactory;
import org.apache.jena.sparql.core.DatasetDescription;

import com.spotify.docker.client.DockerClient;

public class SparqlServiceSupplierDocker
    implements SparqlServiceSupplier
{
    protected DockerClient dockerClient;

    protected String serviceUrl;
    protected DatasetDescription datasetDescription;

    public SparqlServiceSupplierDocker(DockerClient dockerClient) {
        super();
        this.dockerClient = dockerClient;
    }


    @Override
    public SparqlService get() {
        // Start a new container

        SparqlService result = FluentSparqlService.from(
                FluentQueryExecutionFactory.http(serviceUrl,datasetDescription).create(),
                FluentUpdateExecutionFactory.http(serviceUrl, datasetDescription).create());

        // TODO Terminate container on close


        return result;
    }
}

