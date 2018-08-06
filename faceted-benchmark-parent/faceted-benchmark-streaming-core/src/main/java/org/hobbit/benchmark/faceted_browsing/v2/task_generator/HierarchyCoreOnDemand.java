package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.TriplePath;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.E_NotExists;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementFilter;

public class HierarchyCoreOnDemand
	implements HierarchyCore
{
	public static final Var root = Var.alloc("root");
	public static final Var parent = Var.alloc("parent");
	public static final Var ancestor = Var.alloc("ancestor");

	
	protected Path path;
	

	public HierarchyCoreOnDemand(Path path) {
		super();
		this.path = path;
	}

	@Override
	public UnaryRelation roots() {
		UnaryRelation result = createConceptForRoots(path);
		return result;
	}

	/**
	 * Below is wrong:
	 * 
	 * Roots are all nodes having no ancestor without parents
	 * Reason for incorrectness: Consider a cycle "childCycle" which is connected to another cycle "rootCycle".
	 * All members of "childCycle" would be incorrectly classified as roots.
	 * 
	 * 
	 * <pre>
	 * {@code
     * SELECT DISTINCT ?root {
     *   [] <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?root
	 *   FILTER(NOT EXISTS { ?root (<http://www.w3.org/2000/01/rdf-schema#subClassOf>)+ ?ancestor . FILTER(NOT EXISTS {?ancestor <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent }) . })\n" + 
     * }
     * }
     * </pre>
     * 
	 * This means that every node in a cycle which does not have any parent outside of that cycle will become a root.
	 */
	
	
	/**
	 * 
	 * Roots are all nodes for which all parents occur as descendents - a node without a parent trivially satisfies this condition.
	 * Conversely: root nodes are all nodes for which there is no parent that does not occur as a descendent
     * SELECT DISTINCT ?root {
     *   [] <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?root
	 *   FILTER(NOT EXISTS { ?root <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?parent . FILTER(NOT EXISTS {?parent (<http://www.w3.org/2000/01/rdf-schema#subClassOf>)+ ?root }) . })\n" + 
     * }
	 * 
	 * 
	 */
	public static UnaryRelation createConceptForRoots(Path path) {
		Element e = ElementUtils.createElementGroup(
			ElementUtils.createElement(new TriplePath(NodeFactory.createBlankNode(), path, root)),
			new ElementFilter(new E_NotExists(
				ElementUtils.createElementGroup(
					ElementUtils.createElement(new TriplePath(root, path, parent)),
					new ElementFilter(new E_NotExists(ElementUtils.createElementGroup(ElementUtils.createElement(new TriplePath(parent, PathFactory.pathOneOrMore1(path), root)))))
		))));
		
		System.out.println(e);
		
		UnaryRelation result = new Concept(e, root);
		return result;
	}

	/**
	 * If any child is part of a cycle, all members of the cycle become children
	 * 
	 * 
	 * -> so effective children are all direct children that did not already appear as an ancestor
	 */
	@Override
	public UnaryRelation children(UnaryRelation nodes) {
		return null;
	
	}

	@Override
	public UnaryRelation parents(UnaryRelation nodes) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UnaryRelation descendents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UnaryRelation ancestors() {
		// TODO Auto-generated method stub
		return null;
	}	
}