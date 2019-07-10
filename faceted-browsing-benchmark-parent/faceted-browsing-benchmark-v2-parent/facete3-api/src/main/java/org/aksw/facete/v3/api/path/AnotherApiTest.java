package org.aksw.facete.v3.api.path;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;


//
//interface Join {
//	
//}

//class JoinOld {
//	public JoinOld(String lhsAlias, List<Var> lhsVars, String rhsAlias, List<Var> rhsVars) {
//		super();
//		this.lhsAlias = lhsAlias;
//		this.lhsVars = lhsVars;
//
//		this.rhsAlias = rhsAlias;
//		this.rhsVars = rhsVars;
//	}
//
//	String lhsAlias;
//	List<Var> lhsVars;
//
//	String rhsAlias;
//	List<Var> rhsVars;
//	
//	RelationletEntry lhsEntry;
//	RelationletEntry rhsEntry;
//
//	public List<Var> getLhsVars() {
//		return lhsVars;
//	}
//	public List<Var> getRhsVars() {
//		return rhsVars;
//	}
//
//	
//	public String getLhsAlias() {
//		return lhsAlias;
//	}
//	public String getRhsAlias() {
//		return rhsAlias;
//	}
//	
//	
//	
//}







//class RelationletStatic
//	extends RelationletBase
//{
//	protected 
//	
//	@Override
//	public Element getElement() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//	@Override
//	public Set<Var> getVarsMentioned() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//}	


/**
 * If RelationletBinary inherited from Pathlet, we'd have to
 * implement the path-concat operations for it, and the Relationlet would have to be mutable.
 * Because it should be possible for a relationlet to be immutable, it makes sense to have
 * the path-concat ops in a wrapper. Yet, this wrapper should be fairly simple, and the wrapper should not
 * have to manage collections of additions itself. Therefore, we introduce the PathletMember, which
 * wraps a Relationlet, is itself a pathlet (and thus supports path-concat), but it only delegates the
 * ops to a parent container.
 *
 * 
 * 
 * @author raven
 *
 */
//class PathletMemberDeleteme
//	extends RelationletForwarding
////	implements Pathlet//, Relationlet
//{
//	public PathletMemberDeleteme(PathletContainer parent, Relationlet relationlet, Var srcVar, Var tgtVar) {
//		super();
//		this.parent = parent;
//		this.relationlet = relationlet;
//		this.srcVar = srcVar;
//		this.tgtVar = tgtVar;
//	}
//
//	protected PathletContainer parent;
//	protected Relationlet relationlet;
//	
//	protected Var srcVar;
//	protected Var tgtVar;
//
//	@Override
//	protected Relationlet getRelationlet() {
//		return relationlet;
//	}
//
//	@Override
//	public Relationlet materialize() {
//		return this;
//	}
//
////	@Override
////	protected PathletContainer getParent() {
////		return parent;
////	}
////
////	@Override
////	public Var getSrcVar() {
////		return srcVar;
////	}
////
////	@Override
////	public Var getTgtVar() {
////		return tgtVar;
////	}	
//}



//class VarRefPathlet
//	implements VarRefFn
//{
//	protected Pathlet pathlet;
//	protected boolean isTgtVarMode;
//	
//	public VarRefPathlet(Pathlet pathlet, boolean isTgtVarMode) {
//		super();
//		this.pathlet = pathlet;
//		this.isTgtVarMode = isTgtVarMode;
//	}
//
////	@Override
////	public Relationlet getRelationlet() {
////		return pathlet;
////	}
//	
////	@Override
////	public RelationletEntry getEntry() {
////		// TODO Auto-generated method stub
////		return null;
////	}
//
//	@Override
//	public Var getVar() {
//		Var result = isTgtVarMode ? pathlet.getTgtVar() : pathlet.getSrcVar();
//		return result;
//	}
//}


//interface PathletStatic

//interface Nodelet2 {
//	// Get the joins in which this node participates
//	List<Join> getJoins();
//}

/**
 * Initially, a relationlet is a column-less table with a single row, into which members can be
 * joined into.
 * 
 * 
 * @author raven
 *
 */
//interface RelationletJoin {
//	
//	/**
//	 * Add a relationlet as a member.
//	 * Initially, it does not join with anything, hence, it forms a cartesian product with
//	 * the prior members.
//	 * 
//	 * @param alias
//	 * @param relation
//	 */
//	void addMember(String alias, Relationlet relation);
//	
//	Relationlet getEffectiveRelationlet();
//	
//	/**
//	 * SQL-like join operation
//	 * 
//	 * RelationJoin j = r.as("a").joinOn(r.getO()).with(otherRel).as("b").on(otherRel.getS());
//	 * 
//	 * Relation x = r.get("a");
//	 */
//	
//	/**
//	 * Return a new identifier for that member
//	 * 
//	 * 
//	 * @param m
//	 * @return
//	 */
////	String allocateMember(Relationlet m);
////	
////	void addMember(Relationlet m);
////	
////	Relationlet getMember(String label);
////	
////
////	Relationlet union();
//}


//class MappedList<L, T> {
//	protected List<T> items;
//	protected Map<L, Integer> labelToIdx = new LinkedHashMap<>();
//	protected List<Set<L>> idxToLabels;
//	
//	
//	
//	public MappedList(List<T> items, List<Set<L>> idxToLabels) {
//		super();
//		this.items = items;
//		this.labelToIdx = labelToIdx;
//		this.idxToLabels = idxToLabels;
//	}
//
//
//
////	add(L label, T item) {
////		
////	}
//}



// Self-resolving var ref
//interface VarRefFn {
//	//RelationletEntry getEntry();
//	//Relationlet getRelationlet();
//	Var getVar();
//}


//interface MappedElement {
//	
//	List<Element> getElements();
//}
//
//class MappedElementContainer {
//	LinkedHashMap<String, MappedElement> 
//	
//}
//




//class MappedElement {
//	protected Element element;
//	//protected Relation relation;
//	protected Map<Var, Var> exposedVarToElementVar;
//	
//	public MappedElement(Element element, Map<Var, Var> exposedVarToElementVar) {
//		super();
//		this.element = element;
//		this.exposedVarToElementVar = exposedVarToElementVar;
//	}
//
//	public Element getElement() {
//		return element;
//	}
//
//	public Map<Var, Var> getExposedVarToElementVar() {
//		return exposedVarToElementVar;
//	}
//}


//interface CompoundStep {
//	
//}



//interface StepVisitor<T> {
//	<T> visit(StepOptional path);
//	
//}


//
//interface PathBuilder {
//
//	PathBuilder optional(String label);
//	PathBuilder union(String label);
//	PathBuilder fwd(Object property, String label);
//	
//	default PathBuilder optional() {
//		return optional(null);
//	}
//
//	default PathBuilder union() {
//		return optional(null);
//	}
//	
//	default PathBuilder fwd(Object property) {
//		return fwd(property);
//	}
//}



//class PathBuildingRelationlet
//	extends RelationletJoinImpl
//{
//
//}


//class PathBuilderImpl
//	implements PathBuilder
//{
////	protected Relationlet relationlet;
//	protected RelationletJoinImpl relationlet;
//	
//	// References to vars within the relationlet
//	protected VarRef src;
//	protected VarRef tgt;
//	
//	@Override
//	public PathBuilder optional(String label) {
//		Relationlet member = relationlet.getMemberByLabel(label);
//		return null;
//		// Ensure the member type is optional...
//	}
//
//	@Override
//	public PathBuilder union(String label) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public PathBuilder fwd(Object property, String label) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
