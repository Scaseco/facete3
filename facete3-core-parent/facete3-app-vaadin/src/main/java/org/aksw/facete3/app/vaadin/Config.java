package org.aksw.facete3.app.vaadin;

import java.io.IOException;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("facete3")
public class Config {

    private String sparqlEnpoint;
    private final Nli nli = new Nli();
    private Property alternativeLabelProperty; 
    private String prefixFile;

    public String getSparqlEnpoint() {
        return sparqlEnpoint;
    }

    public void setSparqlEnpoint(String sparqlEnpoint) {
        this.sparqlEnpoint = sparqlEnpoint;
    }

    public static class Nli {
        private String endpoint;
        private Long resultLimit;

        public String getEnpoint() {
            return endpoint;
        }

        public Long getResultLimit() {
            return resultLimit;
        }

        public void setResultLimit(Long resultLimit) {
            this.resultLimit = resultLimit;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }
    }

    public Nli getNli() {
        return nli;
    }
    
    public void setAlternativeLabel (String alternativeLabel) {
    	this.alternativeLabelProperty = ResourceFactory.createProperty(alternativeLabel); 
    }
    
    public Property getAlternativeLabel() {
    	return this.alternativeLabelProperty; 
    }
    
    public void setPrefixFile (String prefixFile) throws IOException {
    	this.prefixFile = prefixFile;
    }
    
    public String getPrefixFile (){
    	return this.prefixFile; 
    }
    
}