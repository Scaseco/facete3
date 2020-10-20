package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.vaadin.providers.SearchProvider;
import org.aksw.facete3.app.vaadin.providers.SearchProviderSparql;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

public class ConfigSearchProviderSparql {
    @Bean
    @Autowired
    public SearchProvider searchProvider() {
//        Property property = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/title");
        Property property = RDFS.label;

        return new SearchProviderSparql(searchString -> {
            UnaryRelation r = KeywordSearchUtils.createConceptRegexIncludeSubject(
                    BinaryRelationImpl.create(property), searchString);
            return r;
        });
    }

}
