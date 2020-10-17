package org.aksw.facete3.app.vaadin;

import org.aksw.jena_sparql_api.conjure.datapod.api.RdfDataPod;
import org.aksw.jena_sparql_api.conjure.datapod.impl.DataPods;
import org.aksw.jena_sparql_api.conjure.dataref.rdf.api.DataRefSparqlEndpoint;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

@ImportAutoConfiguration(RefreshAutoConfiguration.class)
public class ConfigEndpoint {

    @Bean
    public DataRefSparqlEndpoint dataRefEndpoint() {
        System.out.println("Created new resource");
        Facete3Wrapper.initJena();

        return ModelFactory.createDefaultModel().createResource().as(DataRefSparqlEndpoint.class);
    }

    @RefreshScope
    @Bean(destroyMethod = "close")
    @Autowired
    public RDFConnection getConnection(DataRefSparqlEndpoint dataRef) {
        RDFConnection result;
        if (dataRef.getServiceUrl() == null) {
            result = RDFConnectionFactory.connect(DatasetFactory.create());
        } else {
            RdfDataPod dataPod = DataPods.fromDataRef(dataRef);
            result = dataPod.openConnection();
        }
        return result;
    }


    @Bean
    @Autowired
    public Runnable test(RDFConnection conn) {
        System.out.println("Creating runnable from connection " + conn);

        return () -> {
            try (QueryExecution qe = conn.query("SELECT (COUNT(*) AS ?c) { ?s a ?o }")) {
                System.out.println(ResultSetFormatter.asText(qe.execSelect()));
            }
        };
    }
}
