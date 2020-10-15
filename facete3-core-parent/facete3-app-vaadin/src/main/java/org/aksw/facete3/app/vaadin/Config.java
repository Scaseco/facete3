package org.aksw.facete3.app.vaadin;

import org.aksw.jena_sparql_api.algebra.transform.TransformExpandAggCountDistinct;
import org.aksw.jena_sparql_api.core.connection.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.sparql.algebra.Transformer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConfigurationProperties("facete3")
public class Config {

    protected String sparqlEnpoint;

    public String getSparqlEnpoint() {
        return sparqlEnpoint;
    }

    public void setSparqlEnpoint(String sparqlEnpoint) {
        this.sparqlEnpoint = sparqlEnpoint;
    }

    @Bean
    public RDFConnection getBaseDataConnection() {
        RDFConnectionBuilder rdfConnectionBuilder = new RDFConnectionBuilder(this);
        RDFConnection rdfConnection = rdfConnectionBuilder.getRDFConnection();

        rdfConnection = RDFConnectionFactoryEx.wrapWithQueryTransform(rdfConnection,
                query -> QueryUtils.applyOpTransform(query,
                        op ->Transformer.transform(new TransformExpandAggCountDistinct(), op)));

        return rdfConnection;
    }
}
