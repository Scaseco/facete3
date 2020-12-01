package org.aksw.facete3.app.vaadin;

import org.aksw.facete3.app.vaadin.plugin.search.SearchPlugin;
import org.aksw.facete3.app.vaadin.plugin.search.SearchPluginImpl;
import org.aksw.facete3.app.vaadin.providers.SearchProviderSparql;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.vocabulary.RDFS;
import org.springframework.context.annotation.Bean;

public class ConfigSearchProviderSparql {

    @Bean
    public SearchPlugin searchPlugin() {
//        Property property = ResourceFactory.createProperty("http://id.loc.gov/ontologies/bibframe/title");
        Property property = RDFS.label;

        return new SearchPluginImpl(
                new SearchProviderSparql(searchString -> {
                    UnaryRelation r;

                    if ("".equals(searchString)) {
                        r = ConceptUtils.createSubjectConcept();
                    } else {
                        r = KeywordSearchUtils.createConceptExistsRegexIncludeSubject(
                                BinaryRelationImpl.create(property), searchString);
                    }

                    return r;
                }),
                null);
    }

}
