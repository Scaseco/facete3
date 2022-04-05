package org.aksw.facete3.app.vaadin.providers;

import java.util.Arrays;
import java.util.function.Function;

import org.aksw.facete3.app.shared.concept.RDFNodeSpec;
import org.aksw.facete3.app.shared.concept.RDFNodeSpecFromRootedQuery;
import org.aksw.facete3.app.shared.concept.RDFNodeSpecFromRootedQueryImpl;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jenax.analytics.core.RootedQuery;
import org.aksw.jenax.analytics.core.RootedQueryFromPartitionedQuery1;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.relation.api.UnaryRelation;
import org.aksw.jenax.sparql.relation.query.PartitionedQuery1;
import org.aksw.jenax.sparql.relation.query.PartitionedQuery1Impl;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.Template;

public class SearchProviderSparql
    implements SearchProvider
{
    protected Function<String, ? extends UnaryRelation> searchStringToConcept;

    public SearchProviderSparql(Function<String, ? extends UnaryRelation> searchStringToConcept) {
        super();
        this.searchStringToConcept = searchStringToConcept;
    }

    @Override
    public RDFNodeSpec search(String searchString) {
        UnaryRelation concept = searchStringToConcept.apply(searchString);

        Element elt = new RelationImpl(
                ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o),
                Arrays.asList(Vars.s, Vars.p, Vars.o))
                .joinOn(Vars.s)
                .with(concept, concept.getVar())
                .getElement();

        Query query = new Query();
        query.setQueryConstructType();
        query.setQueryPattern(elt);
        query.setConstructTemplate(new Template(new BasicPattern()));

        PartitionedQuery1 pq = new PartitionedQuery1Impl(query, concept.getVar());
        RootedQuery rq = new RootedQueryFromPartitionedQuery1(pq);
        RDFNodeSpecFromRootedQuery result = new RDFNodeSpecFromRootedQueryImpl(rq);
        return result;
    }

    @Override
    public String toString() {
        return "sparql";
    }
}
