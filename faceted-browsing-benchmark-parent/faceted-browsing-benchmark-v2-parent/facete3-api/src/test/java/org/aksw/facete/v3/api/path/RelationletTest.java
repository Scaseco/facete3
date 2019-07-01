package org.aksw.facete.v3.api.path;

import java.util.Collections;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

public class RelationletTest {

	@Test
	public void test() {		
		RelationletJoinImpl<Relationlet> child = new RelationletJoinImpl<>();
		child.add("a", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDF.type.asNode(), Vars.o)).setVarFixed(Vars.s, true));
		child.add("b", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDFS.label.asNode(), Vars.o)));
		child.expose("u", "a", "s");
		
		RelationletJoinImpl<Relationlet> parent = new RelationletJoinImpl<>();
		parent.add("a", child);
		parent.add("c", Relationlets.from(ElementUtils.createElementTriple(Vars.x, RDFS.seeAlso.asNode(), Vars.y)));
		parent.addJoin("a", Collections.singletonList(Vars.u), "c", Collections.singletonList(Vars.x));
			
		RelationletNested me = parent.materialize();
		System.out.println("Final element:\n" + me.getElement());
		
//		System.out.println("finalVar: "  + me.getExposedVarToElementVar().get(Var.alloc("foo")));
//		System.out.println("finalVar: "  + me.getExposedVarToElementVar());
		System.out.println("finalVar: "  + me.getNestedVarMap());


	}

}
