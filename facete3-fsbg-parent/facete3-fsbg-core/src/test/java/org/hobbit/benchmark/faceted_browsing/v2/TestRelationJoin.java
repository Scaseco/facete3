package org.hobbit.benchmark.faceted_browsing.v2;

import org.aksw.jenax.sparql.fragment.api.Fragment;
import org.aksw.jenax.sparql.fragment.api.Fragment2;
import org.aksw.jenax.sparql.fragment.impl.FragmentUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class TestRelationJoin {

    // Test of the join API to use variables from both operands
    @Test
    public void testRelationJoin() {
        Fragment2 a = FragmentUtils.createRelation(RDFS.seeAlso, false);
        Fragment2 b = FragmentUtils.createRelation(RDF.type, false);

        Fragment result = //RelationJoiner.join(a.getElement(), b.getElement())
            a.joinOn(a.getTargetVar())
            .projectSrcVars(a.getSourceVar())
            .projectTgtVars(b.getTargetVar())
            .with(b, b.getSourceVar());

        System.out.println(result);
    }
}
