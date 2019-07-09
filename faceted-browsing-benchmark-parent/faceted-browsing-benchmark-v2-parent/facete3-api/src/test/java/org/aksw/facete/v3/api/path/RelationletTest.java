package org.aksw.facete.v3.api.path;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class RelationletTest {

	@Test
	public void test() {		
		RelationletJoinImpl<Relationlet> child = new RelationletJoinImpl<>();
		child.add("a", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDF.type.asNode(), Vars.o)).fix(Vars.s));
		child.add("b", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDFS.label.asNode(), Vars.o)));
		child.expose("u", "a", "s");
		child.addJoin("a", Arrays.asList(Vars.s), "b", Arrays.asList(Vars.s));		
		
		RelationletJoinImpl<Relationlet> parent = new RelationletJoinImpl<>();
		parent.add("a", child);
		parent.add("c", Relationlets.from(new ElementOptional(ElementUtils.createElementTriple(Vars.x, RDFS.seeAlso.asNode(), Vars.y))));
		parent.addJoin("a", Collections.singletonList(Vars.u), "c", Collections.singletonList(Vars.x));
			
		RelationletNested me = parent.materialize();
		System.out.println("Final element:\n" + me.getElement());
		
//		System.out.println("finalVar: "  + me.getExposedVarToElementVar().get(Var.alloc("foo")));
//		System.out.println("finalVar: "  + me.getExposedVarToElementVar());
		System.out.println("finalVar: "  + me.getNestedVarMap());

		NestedVarMap tmp1 = me.getNestedVarMap().get(Arrays.asList("a", "a"));
		Map<Var, Var> tmp2 = tmp1.getLocalToFinalVarMap();
		
		// expected: ?s
		System.out.println("Effective var for a.a.s: " + tmp2.get(Vars.s));
		
		// expected: ?s
		System.out.println("Effective var for a.u " + me.getNestedVarMap().get(Arrays.asList("a")).getLocalToFinalVarMap().get(Vars.u)); 

	}

}
