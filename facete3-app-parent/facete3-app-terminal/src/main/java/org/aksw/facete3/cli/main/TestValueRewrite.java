package org.aksw.facete3.cli.main;

import java.util.Collection;

import org.aksw.jena_sparql_api.algebra.utils.VirtualPartitionedQuery;
import org.aksw.jenax.arq.util.syntax.ElementUtils;
import org.aksw.jenax.arq.util.var.Vars;
import org.aksw.jenax.sparql.fragment.api.Fragment3;
import org.aksw.jenax.sparql.fragment.impl.Fragment3Impl;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;

public class TestValueRewrite {
    public static void main(String[] args) {
        Collection<Fragment3> views = VirtualPartitionedQuery.toViews(QueryFactory.create("CONSTRUCT { ?s <urn:score> ?o } { VALUES (?s ?o) { ( <urn:s> 1.0 ) } }"));

        views.add(new Fragment3Impl(ElementUtils.createElementTriple(Vars.s, Vars.p, Vars.o), Vars.s, Vars.p, Vars.o));


        Query result = VirtualPartitionedQuery.rewrite(views, QueryFactory.create("SELECT * { ?s ?a <urn:Foo> . OPTIONAL { ?s <urn:score> ?o } } ORDER BY ?o"));
        System.out.println(result);
    }
}
