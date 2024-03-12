package org.hobbit.benchmark.faceted_browsing.v2.main;

import org.aksw.jena_sparql_api.core.SparqlServiceReference;
import org.aksw.jenax.connection.extra.RDFConnectionFactoryEx;
import org.aksw.jenax.connection.extra.RDFConnectionMetaData;
import org.aksw.jenax.dataaccess.sparql.datasource.RdfDataSource;
import org.aksw.jenax.reprogen.core.JenaPluginUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sys.JenaSystem;


public class DatasetCache {
    public static void main(String[] args) throws Exception {
        JenaSystem.init();

        JenaPluginUtils.registerResourceClasses(RDFConnectionMetaData.class);

        // The workflow we want to implement is:
        // 1. Create a RDFConnection to a SPARQL endpoint
        // 2. Create a derived dataset by executing a sequence of SPARQL construct queries
        //    from .sparql file against it
        // 3. Cache the resulting dataset
        String serviceUrl = "http://localhost:8890/sparql";
        DatasetDescription dd = new DatasetDescription();
        dd.addDefaultGraphURI("http://dbpedia.org");

        SparqlServiceReference ssr = new SparqlServiceReference(serviceUrl, dd);

        RdfDataSource dataSource = () -> RDFConnectionFactoryEx.connect(ssr);

        Model model = new RdfWorkflowSpec()
            //.setDefaultConnectionFactory(FluentQueryExecutionFactory::connect)
            .deriveDatasetWithSparql(dataSource, "analyze-numeric-properties.sparql")
            .cache(true)
            .getModel();

//		Model model = RdfWorkflowRunner
//			.loadModel(spec);

    }
}


