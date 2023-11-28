package org.aksw.facete3.app.vaadin;

import java.util.Arrays;
import java.util.Collection;

import org.aksw.commons.util.function.FixpointIteration;
import org.aksw.facete3.app.vaadin.plugin.search.SearchPlugin;
import org.aksw.facete3.app.vaadin.plugin.search.SearchPluginImpl;
import org.aksw.facete3.app.vaadin.providers.SearchProviderNli;
import org.aksw.jena_sparql_api.algebra.transform.TransformDistributeJoinOverUnion;
import org.aksw.jena_sparql_api.algebra.transform.TransformEvalTable;
import org.aksw.jena_sparql_api.algebra.transform.TransformFactorizeTableColumnsToExtend;
import org.aksw.jena_sparql_api.algebra.utils.VirtualPartitionedQuery;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jenax.arq.util.query.QueryTransform;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.syntax.QueryUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.dataaccess.sparql.connection.common.RDFConnectionBuilder;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.aksw.jenax.sparql.fragment.impl.Fragment3Impl;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.TableFactory;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.syntax.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.collect.Lists;

/**
 * The config class sets up a connection to the aksw/cord natural language interface (nli)
 * service and adds a connection pool mutator that applies query rewriting to introduce a virtual
 * 'score' property.
 *
 * @author raven
 *
 */
@Configuration
public class ConfigSearchProviderNli {
    private static final Logger logger = LoggerFactory.getLogger(ConfigSearchProviderNli.class);


//    @Bean
//    @ConfigurationProperties("facete3.nli")
//    public NliConfig getNliConfig() {
//        return new NliConfig();
//    }

    @Bean
    @Autowired
    public SearchPlugin searchPlugin(NliConfig nliConfig) {
        SearchPlugin result = new SearchPluginImpl(
                new SearchProviderNli(nliConfig),
                createConnectionTransformer(nliConfig));

        return result;
    }


    public static SearchSensitiveRDFConnectionTransform createConnectionTransformer(NliConfig nliConfig) {
        logger.info("Natural Language Interface (NLI) Service: " + nliConfig.getEndpoint());
        return rdfNodeSpec -> {
            Table table = TableFactory.create(Arrays.asList(Vars.s, Vars.o));
            for(RDFNode rdfNode : rdfNodeSpec.getCollection()) {
                if (rdfNode.isResource()) {
                    Resource r = rdfNode.asResource();
                    RDFNode score = ResourceUtils.getPropertyValue(r, SearchProviderNli.score);
                    if (score != null) {

                        BindingBuilder bb = BindingFactory.builder();
                        bb.add(Vars.s, r.asNode());
                        bb.add(Vars.o, score.asNode());
                        table.addBinding(bb.build());
                    }
                }
            }

            BasicPattern bgp = new BasicPattern();
            bgp.add(Triple.create(Vars.s, SearchProviderNli.score.asNode(), Vars.o));

            Query view = new Query();
            view.setQueryConstructType();
            view.setConstructTemplate(new Template(bgp));
            view.setQueryPattern(ElementUtils.createElementData(table.getVars(), Lists.newArrayList(table.rows())));


            Collection<Fragment3> views = VirtualPartitionedQuery.toViews(view);
            views.add(new Fragment3Impl(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o), Vars.s, Vars.p, Vars.o));

            QueryTransform queryTransform = query -> {
//                System.out.println("Before rewrite: " + query);
                Query raw = VirtualPartitionedQuery.rewrite(views, query);
//                System.out.println("After rewrite: " + raw);
                // Evaluate 'static' parts of the query - such as operations based on OpTable - directly

                Query tmp = QueryUtils.applyOpTransform(raw,
                        FixpointIteration.createClosure(op -> Transformer.transform(new TransformDistributeJoinOverUnion(), op)));
//                System.out.println("After join-over-union distribution: " + tmp);

                // TODO Due to a virtuoso bug with unions involving VALUES
                // TransformEvalTable may cause loss of result bindings...
                // Consider a workaround...

                Query r = QueryUtils.applyOpTransform(tmp,
                        op -> Transformer.transform(TransformEvalTable.create(), op));

//                System.out.println("Before factorization: " + r);

                r = QueryUtils.applyOpTransform(r,
                        op -> Transformer.transform(new TransformFactorizeTableColumnsToExtend(), op));

//                System.out.println("After optimization: " + r);
                return r;
            };

            return conn ->
                RDFConnectionBuilder.from(conn).addQueryTransform(queryTransform).getConnection();
        };
    }

}
