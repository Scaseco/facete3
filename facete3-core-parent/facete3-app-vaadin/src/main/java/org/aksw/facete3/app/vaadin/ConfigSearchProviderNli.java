package org.aksw.facete3.app.vaadin;

import java.util.Arrays;
import java.util.Collection;

import org.aksw.facete3.app.vaadin.providers.SearchProvider;
import org.aksw.facete3.app.vaadin.providers.SearchProviderNli;
import org.aksw.jena_sparql_api.algebra.utils.VirtualPartitionedQuery;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.core.QueryTransform;
import org.aksw.jena_sparql_api.core.connection.RDFConnectionBuilder;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * The config class sets up a connection to the aksw/cord natural language interface (nli)
 * service and adds a connection pool mutator that applies query rewriting to introduce a virtual
 * 'score' property.
 *
 * @author raven
 *
 */
public class ConfigSearchProviderNli {
    @ConfigurationProperties("facete3.nli")
    public static class NliConfig {
        private String endpoint;
        private Long resultLimit;

        public String getEndpoint() {
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

//    @Bean
//    @ConfigurationProperties("facete3.nli")
//    public NliConfig getNliConfig() {
//        return new NliConfig();
//    }


    @Bean
    @Autowired
    public SearchSensitiveRDFConnectionTransform connectionTransformer(NliConfig nliConfig) {
        System.out.println("NLI SERVICE: " + nliConfig.getEndpoint());
        return rdfNodeSpec -> {
            Table table = TableFactory.create(Arrays.asList(Vars.s, Vars.o));
            for(RDFNode rdfNode : rdfNodeSpec.getCollection()) {
                if (rdfNode.isResource()) {
                    Resource r = rdfNode.asResource();
                    RDFNode score = ResourceUtils.getPropertyValue(r, SearchProviderNli.score);
                    if (score != null) {

                        BindingMap b = BindingFactory.create();
                        b.add(Vars.s, r.asNode());
                        b.add(Vars.o, score.asNode());
                        table.addBinding(b);
                    }
                }
            }

            BasicPattern bgp = new BasicPattern();
            bgp.add(new Triple(Vars.s, SearchProviderNli.score.asNode(), Vars.o));

            Query view = new Query();
            view.setQueryConstructType();
            view.setConstructTemplate(new Template(bgp));
            view.setQueryPattern(new ElementData(table.getVars(), Lists.newArrayList(table.rows())));


            Collection<TernaryRelation> views = VirtualPartitionedQuery.toViews(view);
            views.add(new TernaryRelationImpl(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o), Vars.s, Vars.p, Vars.o));

            QueryTransform queryTransform = query -> {
                Query r = VirtualPartitionedQuery.rewrite(views, query);
                return r;
            };

            return conn ->
                RDFConnectionBuilder.from(conn).addQueryTransform(queryTransform).getConnection();
        };
    }


    @Bean
    @Autowired
    public SearchProvider searchProvider(NliConfig nliConfig) {
        return new SearchProviderNli(nliConfig);
    }
}
