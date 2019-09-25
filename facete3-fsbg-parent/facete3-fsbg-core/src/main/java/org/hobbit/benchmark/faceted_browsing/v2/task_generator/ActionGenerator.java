package org.hobbit.benchmark.faceted_browsing.v2.task_generator;

import java.util.Set;

import org.aksw.facete.v3.api.FacetedQuery;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.impl.ResourceImpl;


/**
 * Fluent API for generating faceted browsing constraints - i.e.
 * (todo how to represent the constraints? pair of path - restriction?)
 * 
 * The API enables expressing requirements generated constraints must meet, such as
 * "numer range with upper bound".
 * 
 * 
 * @author Claus Stadler, Sep 19, 2018
 *
 */
public class ActionGenerator {
	protected FacetedQuery query;
	
		
}



interface Edge<V extends RDFNode>
	extends Resource
{
	V getSource();
	V getTarget();
}

/**
 * 
 * @author Claus Stadler, Sep 19, 2018
 *
 * @param <V>
 */
interface Transition<V extends State>
	extends Edge<V>
{
	RDFNode getSymbol();
	// TODO We could extend the proxy util to recognize this signature
	<S> S getSymbol(Class<S> literalClass);
//	<S extends RDFNode> S getResourceSymbol(Class<S> resourceClass);
}


/**
 * Transition with an extra weight attribute
 * 
 * @author Claus Stadler, Sep 19, 2018
 *
 */
interface ProbabilisticTransition
	extends Transition
{
	
}

/**
 * A state is a resource with transitions.
 * A state is assumed to belong only to a single automaton, so that
 * any information, such as starting / final predicates, are meaningful.
 * 
 * 
 * @author Claus Stadler, Sep 19, 2018
 *
 * @param <T>
 */
interface State
	extends Resource
{
	Set<?> getTransitions();
}

class ProbabilisticAutomaton {

}

/**
 * A simple path specification.
 * 
 * In general, we could use a complete probabilistic automaton...
 * So if we used an Automaton, then we should just model it all with RDF.
 * 
 * 
 * @author Claus Stadler, Sep 19, 2018
 *
 */
class PathSpecSimpleX
	extends ResourceImpl
{

	ProbabilisticAutomaton pathModel;
//	protected int minLength;
//	protected int maxLength;
//	protected Model

}

//class RangeSpec {
//	protected boolean upperBound;
//	protected boolean lowerBound;
//	
//	RangeSpec withUpperBound() {
//		this.upperBound = true;
//		return this;
//	}
//
//	RangeSpec withLowerBound() {
//		this.lowerBound = true;
//		return this;
//	}
//	
//	
//}
