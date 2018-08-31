package org.aksw.facete.v3.bgp.api;

import java.util.Map;
import java.util.Optional;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Link;
import org.apache.jena.sparql.path.P_ReverseLink;
import org.apache.jena.sparql.path.P_Seq;
import org.apache.jena.sparql.path.Path;
import org.hobbit.benchmark.faceted_browsing.v2.domain.Vocab;

public interface BgpNode
	extends Resource
{	
	BgpDirNode fwd();
	BgpDirNode bwd();

	Map<Resource, BgpMultiNode> fwdMultiNodes();
	Map<Resource, BgpMultiNode> bwdMultiNodes();
	
	
	// Convenience shortcuts
	default BgpMultiNode fwd(Property property) {
		return fwd().via(property);
	}
	
	default BgpMultiNode bwd(Property property) {
		return bwd().via(property);
	}

	default BgpMultiNode fwd(String p) {
		Property property = ResourceFactory.createProperty(p);
		return fwd().via(property);
	}

	default BgpMultiNode bwd(String p) {
		Property property = ResourceFactory.createProperty(p);
		return bwd().via(property);
	}
	
	default BgpMultiNode fwd(Node node) {
		return fwd().via(ResourceFactory.createProperty(node.getURI()));
	}

	default BgpMultiNode bwd(Node node) {
		return bwd().via(ResourceFactory.createProperty(node.getURI()));
	}

	
	default BgpMultiNode nav(String p, boolean reverse) {
		return reverse ? bwd(p) : fwd(p);
	}

	default BgpMultiNode nav(Node p, boolean reverse) {
		return reverse ? bwd(p) : fwd(p);
	}

	default BgpMultiNode nav(Property p, boolean reverse) {
		return reverse ? bwd(p) : fwd(p);
	}

	
	default BgpNode nav(Path path) {
		BgpNode result;
		if(path == null) {
			result = this;
		} else if(path instanceof P_Seq) {
			P_Seq seq = (P_Seq)path;
			result = nav(seq.getLeft()).nav(seq.getRight());
		} else if(path instanceof P_Link) {
			P_Link link = (P_Link)path;
			result = fwd(link.getNode()).one();
		} else if(path instanceof P_ReverseLink) {
			P_Link reverseLink = (P_Link)path;
			result = bwd(reverseLink.getNode()).one();
		} else {
			throw new IllegalArgumentException("Unsupported path type " + path + " " + Optional.ofNullable(path).map(Object::getClass).orElse(null));
		}
		
		return result;
	}
	

	BgpNode as(String varName);
	BgpNode as(Var var);
	Var alias();


	BgpMultiNode parent();
	//BgpNode parent(BgpMultiNode parent);

	//BinaryRelation getReachingRelation();
	
	BgpNode root();
	
	/** Get the set of simple constraints affecting this facet.
	 * Simple constraints are expressions making use of only a single variable.
	 * The set of constraints is treated as a disjunction */
	//Set<Expr> getConstraints();
	
	
	
	public static BinaryRelation getReachingRelation(BgpNode state) {
		BinaryRelation result;

		BgpMultiNode parent = state.parent();
		if(parent == null) {
			result = null;
		} else {
			
//			boolean isReverse = false;
//			Resource entry = ResourceUtils.tryGetReversePropertyValue(parent, Vocab.fwd)
//				.orElseGet(() -> ResourceUtils.getReversePropertyValue(parent, Vocab.bwd));
//
//			Resource p = ResourceUtils.getPropertyValue(entry, Vocab.property, Resource.class);

			Resource p = parent.reachingProperty();
			boolean isReverse = parent.isReverse();
			
			//Set<Statement> set = ResourceUtils.listProperties(parent, null).filterKeep(stmt -> stmt.getObject().equals(state)).toSet();
//			
//			if(set.isEmpty()) {
//				isReverse = true;
//				set = ResourceUtils.listReverseProperties(parent, null).filterKeep(stmt -> stmt.getSubject().equals(state)).toSet();
//			}
//
//			// TODO Should never fail - but ensure that
//			Property p = set.iterator().next().getPredicate();
//
			result = create(p.asNode(), isReverse);
		}

		return result;		
	}
	
	public static BinaryRelation create(Node node, boolean isReverse) {
		//ElementUtils.createElement(triple)
		Triple t = isReverse
				? new Triple(Vars.o, node, Vars.s)
				: new Triple(Vars.s, node, Vars.o);

		BinaryRelation result = new BinaryRelationImpl(ElementUtils.createElement(t), Vars.s, Vars.o);
		return result;
	}

}
