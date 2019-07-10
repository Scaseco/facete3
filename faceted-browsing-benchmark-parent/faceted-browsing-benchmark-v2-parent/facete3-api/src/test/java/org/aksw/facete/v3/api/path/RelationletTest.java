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
	public void testJoins() {
		RelationletJoinImpl<Relationlet> joiner = new RelationletJoinImpl<>();
		
		
		
		if(false) {
			joiner.add("a", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDF.type.asNode(), Vars.o)).setVarFixed(Vars.s, true));
			joiner.add("b", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDFS.label.asNode(), Vars.o)));
			joiner.add("c", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDFS.isDefinedBy.asNode(), Vars.y)));
			
			joiner.addJoin("a", Collections.singletonList(Vars.o), "b", Collections.singletonList(Vars.s));
			joiner.addJoin("b", Collections.singletonList(Vars.o), "c", Collections.singletonList(Vars.s));
			
			joiner.materialize();
		}

		if(false) {
			// Corner case: two independent joins are subsequently affected by another join
			// A.w B.x C.y D.z
			// A.w = B.x
			// C.y = D.z
			// A.w = C.y

			joiner.add("a", Relationlets.from(ElementUtils.createElementTriple(Vars.w, Vars.p, Vars.o)).setVarFixed(Vars.p, true));
			joiner.add("b", Relationlets.from(ElementUtils.createElementTriple(Vars.x, Vars.p, Vars.o)).setVarFixed(Vars.x, true));
			joiner.add("c", Relationlets.from(ElementUtils.createElementTriple(Vars.y, Vars.p, Vars.i)).setVarFixed(Vars.p, true));
			joiner.add("d", Relationlets.from(ElementUtils.createElementTriple(Vars.z, Vars.p, Vars.o)).setVarFixed(Vars.o, true));
			
			joiner.addJoin("a", Collections.singletonList(Vars.w), "b", Collections.singletonList(Vars.x));
			joiner.addJoin("c", Collections.singletonList(Vars.y), "d", Collections.singletonList(Vars.z));
			joiner.addJoin("a", Collections.singletonList(Vars.w), "c", Collections.singletonList(Vars.y));

			//joiner.addJoin("a", Collections.singletonList(Vars.w), null, Collections.singletonList(Vars.y));

			joiner.expose("foo", "a", "w");
			joiner.expose("bar", "b", "x");
			RelationletNested me = joiner.materialize();
//			System.out.println("finalVar: "  + me.getExposedVarToElementVar().get(Var.alloc("foo")));
//			System.out.println("finalVar: "  + me.getExposedVarToElementVar());
			System.out.println("finalVar: "  + me.getNestedVarMap());
			
		}

		
		if(true) {
			
			Path commonParentPath = Path.newPath().fwd(RDF.type);

			Path p1 = commonParentPath.fwd(RDFS.label, "p1");
			Path p2 = commonParentPath.fwd(RDFS.label, "p2");

			Path px = Path.newPath().fwd(RDF.type);

			PathletContainerImpl pathlet = new PathletContainerImpl();
			pathlet.resolvePath(px);
			pathlet.resolvePath(p1);
			//pathlet.resolvePath(p2);
//			pathlet.resolvePath(px);
			
			RelationletNested rn = pathlet.materialize();
			System.out.println("Materialized Element: " + rn.getElement());
			System.out.println("Materialized Vars    : " + rn.getExposedVars());
			
			//p1.resolveIn(pathlet);
			
//			pathBuilder.optional()
			
		}
		
	}
	
	
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
