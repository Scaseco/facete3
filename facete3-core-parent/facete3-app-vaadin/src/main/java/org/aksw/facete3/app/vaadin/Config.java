package org.aksw.facete3.app.vaadin;

import org.apache.jena.rdfconnection.RDFConnection;
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
        return rdfConnection;
    }
}
