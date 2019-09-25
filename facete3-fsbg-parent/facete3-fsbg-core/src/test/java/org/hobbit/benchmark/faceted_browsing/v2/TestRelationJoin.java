package org.hobbit.benchmark.faceted_browsing.v2;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class TestRelationJoin {

	// Test of the join API to use variables from both operands
	@Test
	public void testRelationJoin() {
		BinaryRelation a = RelationUtils.createRelation(RDFS.seeAlso, false);
		BinaryRelation b = RelationUtils.createRelation(RDF.type, false);

		Relation result = //RelationJoiner.join(a.getElement(), b.getElement())
			a.joinOn(a.getTargetVar())
			.projectSrcVars(a.getSourceVar())
			.projectTgtVars(b.getTargetVar())
			.with(b, b.getSourceVar());
		
		System.out.println(result);
	}
}
