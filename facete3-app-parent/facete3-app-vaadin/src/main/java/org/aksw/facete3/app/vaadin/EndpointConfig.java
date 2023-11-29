package org.aksw.facete3.app.vaadin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("facete3")
public class EndpointConfig {
    private static final Logger logger = LoggerFactory.getLogger(EndpointConfig.class);

    protected String sparqlEndpoint;

    public String getSparqlEndpoint() {
        return sparqlEndpoint;
    }

    public void setSparqlEndpoint(String sparqlEndpoint) {
        if (logger.isInfoEnabled()) {
            logger.info("Sparql endpoint set to: " + sparqlEndpoint);
        }
        this.sparqlEndpoint = sparqlEndpoint;
    }
}
