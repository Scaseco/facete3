package org.aksw.facete3.app.vaadin;

import org.aksw.jena_sparql_api.algebra.transform.TransformExpandAggCountDistinct;
import org.aksw.jena_sparql_api.core.connection.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Transformer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@ConfigurationProperties("facete3")
public class Config {

    protected String sparqlEnpoint;
    protected Property alternativeLabelProperty;

    protected String[] prefixSources;

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

    public void setAlternativeLabel (String alternativeLabel) {
        this.alternativeLabelProperty = ResourceFactory.createProperty(alternativeLabel);
    }

    public Property getAlternativeLabel() {
        return this.alternativeLabelProperty;
    }

    @Bean
    public PrefixMapping globalPrefixMapping() {
        PrefixMapping result = new PrefixMappingImpl();
        for (String prefixSource : prefixSources) {
            Model model = RDFDataMgr.loadModel(prefixSource);
            result.setNsPrefixes(model);
        }
        return result;
    }

    public void setPrefixSources(String[] prefixSources) {
        this.prefixSources = prefixSources;
    }

    public String[] getPrefixSources(){
        return prefixSources;
    }

}