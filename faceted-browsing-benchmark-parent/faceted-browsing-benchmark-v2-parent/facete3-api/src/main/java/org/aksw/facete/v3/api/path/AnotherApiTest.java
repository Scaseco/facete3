package org.aksw.facete.v3.api.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.collections.collectors.CollectorUtils;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;




class ElementInstance {
	Element element;
	String label;
}


//
//interface Join {
//	
//}

class Join {
	protected List<VarRef> lhs;
	protected List<VarRef> rhs;
	
	public Join(List<VarRef> lhs, List<VarRef> rhs) {
		super();
		this.lhs = lhs;
		this.rhs = rhs;
	}

	public List<VarRef> getLhs() {
		return lhs;
	}

	public List<VarRef> getRhs() {
		return rhs;
	}
}


class JoinOld {
	public JoinOld(String lhsAlias, List<Var> lhsVars, String rhsAlias, List<Var> rhsVars) {
		super();
		this.lhsAlias = lhsAlias;
		this.lhsVars = lhsVars;

		this.rhsAlias = rhsAlias;
		this.rhsVars = rhsVars;
	}

	String lhsAlias;
	List<Var> lhsVars;

	String rhsAlias;
	List<Var> rhsVars;
	
	RelationletEntry lhsEntry;
	RelationletEntry rhsEntry;

	public List<Var> getLhsVars() {
		return lhsVars;
	}
	public List<Var> getRhsVars() {
		return rhsVars;
	}

	
	public String getLhsAlias() {
		return lhsAlias;
	}
	public String getRhsAlias() {
		return rhsAlias;
	}
	
	
	
}



/**
 * Reference to a variable in a relationlet - can be based on lambdas
 * @author raven
 *
 */
interface VarRef {
//	Var getVar();
//	RelationletEntry getEntry();
}


class Pathlets {
//	VarRef srcVarRef(Pathlet pathlet) {
////		return new VarRef() {
////			
////		};
//	}
	
	
}


interface Pathlet
	extends Relationlet
{
	Var getSrcVar();
	Var getTgtVar();
	
}

/**
 * A pathlet is a relationlet with designated source and target variables plus
 * operators for concatenation of pathlets
 * 
 * @author raven
 *
 */
interface IPathletContainer
	extends Pathlet
{
	/**
	 * Add a left-join
	 * 
	 * { // ElementGroup
	 *   lhs
	 *   OPTIONAL {
	 *     rhs
	 *   }
	 * }
	 * 
	 * @return
	 */
	Pathlet optional(Pathlet rhs);
	
	// get or create an optional block with the given label
	Pathlet optional(String label);
	
	// get or create an optional block with a null label
    default Pathlet optional() {
    	return optional((String)null);
    }
	
    static Pathlet as(String alias) {
	    return null;
    }
}


// Probably we need to distinguish between simple relationlets with 'constant' vars
// and those with dynamic vars, which means, that variable referred to by a varref can change 
class RelationletBase
	implements Relationlet
{
	protected Set<Var> fixedVars = new LinkedHashSet<>();
	protected Set<Var> exposedVars = new LinkedHashSet<>();

	@Override
	public Relationlet getMember(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Var getInternalVar(Var var) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Var> getExposedVars() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Var> getFixedVars() {
		return fixedVars;
	}

	@Override
	public Set<Var> getVarsMentioned() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Relationlet setVarFixed(Var var, boolean onOrOff) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element getElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RelationletNested materialize() {
		// TODO Auto-generated method stub
		return null;
	}
	

//	@Override
//	public Relationlet setVarFixed(Var var, boolean onOrOff) {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
}


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


class RelationletBinary
	extends RelationletBase
	//implements Pathlet
{
	protected BinaryRelation br;
	
	public RelationletBinary(BinaryRelation br) {
		super();
		this.br = br;
	}

	@Override
	public Element getElement() {
		return br.getElement();
	}
//
//	@Override
//	public Var getSrcVar() {
//		return br.getSourceVar();
//	}
//
//	@Override
//	public Var getTgtVar() {
//		return br.getTargetVar();
//	}
}

abstract class RelationletForwarding
	implements Relationlet
{
	protected abstract Relationlet getRelationlet();
	

	@Override
	public Relationlet getMember(String alias) {
		return getRelationlet().getMember(alias);
	}

	@Override
	public Var getInternalVar(Var var) {
		return getRelationlet().getInternalVar(var);
	}

	@Override
	public Collection<Var> getExposedVars() {
		return getRelationlet().getExposedVars();
	}

	@Override
	public Set<Var> getVarsMentioned() {
		return getRelationlet().getVarsMentioned();
	}

	@Override
	public Set<Var> getFixedVars() {
		return getRelationlet().getFixedVars();
	}

	@Override
	public Relationlet setVarFixed(Var var, boolean onOrOff) {
		return getRelationlet().setVarFixed(var, onOrOff);
	}

	@Override
	public Element getElement() {
		return getRelationlet().getElement();
	}

	@Override
	public RelationletNested materialize() {
		return getRelationlet().materialize();
	}

}


class PathletSimple
	extends RelationletForwarding
	implements Pathlet
{
	protected Var srcVar;
	protected Var tgtVar;
	protected Relationlet relationlet;
	
	public PathletSimple(Var srcVar, Var tgtVar, Relationlet relationlet) {
		super();
		this.srcVar = srcVar;
		this.tgtVar = tgtVar;
		this.relationlet = relationlet;
	}

	@Override
	protected Relationlet getRelationlet() {
		return relationlet;
	}

	@Override
	public Var getSrcVar() {
		return srcVar;
	}

	@Override
	public Var getTgtVar() {
		return tgtVar;
	}
}

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



interface MemberKey {
	
}




class PathletContainer
	extends RelationletJoinImpl<Pathlet>
	implements IPathletContainer
{
//	protected Table<MemberKey, String, PathletMember> keyToAliasToMember;
	protected Table<Object, String, Pathlet> keyToAliasToMember = HashBasedTable.create();
	
	
	//protected PathletContainer container;
	protected Function<Element, Element> elementPostProcessor;
	
	// All element-creating methods connect to this variable
	protected Var connectorVar;
	
	
	// sourceVar / tgtVar...?
	
	public PathletContainer(PathletContainer container, Function<Element, Element> elementPostProcessor) {
		super();
		//this.container = container;
		this.elementPostProcessor = elementPostProcessor;
	}

	PathletContainer resolvePath(Path path) {
		List<Step> steps = new ArrayList<>();
		Path c = path;
		do {
			Step step = c.getStep();
			if(step != null) {
				steps.add(step);
			}
			c = c.getParent();
		} while(c != null);

		Collections.reverse(steps);

		PathletContainer result = resolve(steps.iterator());
		return result;
	}
	
	
	PathletContainer resolveStep(Step step) {
		PathletContainer result;
		
		String type = step.getType();
		String alias = step.getAlias();
		Object key = step.getKey();

		switch(type) {
		case "optional":
			result = optional(alias);
			break;
		case "br":
			result = fwd(key, alias);
			break;
		default:
			throw new RuntimeException("Unknown step type " + type);
		}
		//optional()
	
		return result;
	}
	

	
	PathletContainer resolve(Iterator<Step> it) {
		PathletContainer result = this;
		while(it.hasNext()) {
			Step step = it.next();
			result = result.resolveStep(step);
			Objects.requireNonNull(result, "Step resolution unexpectedly returned null");
		}
//		} else {
//			result = this;
//		}
//		
		return result;
	}

//	RelationletEntry add(Pathlet pathlet) {
//		return super.add(pathlet);
//	}
	
	Pathlet getMember(Object key, String alias) {
		Pathlet member = keyToAliasToMember.get(key, alias);
		//PathletContainer result = member.
		return member;
	}

	Pathlet add(Object key, String alias, Pathlet pathlet) {
		return null;
	}

	PathletContainer fwd(Object key, String alias) {
		BinaryRelation br = (BinaryRelation)key;
		
		//BinaryRelation br = RelationUtils.createRelation(p, false, null)
		return fwd(null, br, alias);
	}
	
	PathletContainer fwd(Object key, BinaryRelation br, String alias) {
		alias = alias == null ? "default" : alias;

		key = key == null ? "" + br : key;
		
		//BinaryRelation br = RelationUtils.createRelation(p, false, null);
		// Check if there is a member with this relation pattern already
		
		
		PathletContainer result = (PathletContainer)getMember(br, alias);//members.find(m -> m.getPattern().equals(br.getElement()));

		if(result == null) {
			result = new PathletContainer(this, e -> e);
//			RelationletBinary r = new RelationletBinary(br);
//			//PathletMember childContainerMember = new PathletMember(childContainer, r, r.getSrcVar(), r.getTgtVar());
//			
//			
//			this.add(br, alias, childContainer);
			
			// Set up a join of this node with the newly created member
			//childContainerMember.
			result.add("root", new PathletSimple(
					br.getSourceVar(), br.getTargetVar(),
					new RelationletBinary(br)));
			result.expose("joinSrc", "root", "s");
			result.expose("joinTgt", "root", "o");
			
			keyToAliasToMember.put(key, alias, result);
			RelationletEntry<Pathlet> e = this.add(result);
			String el = e.getId();//this.getLabelForId(e.getId());
			
			
			// Join this pathlet's joinTgt with the joinSrc of the member 
			this.addJoin("root", Arrays.asList(Var.alloc("joinTgt")), el, Arrays.asList(Var.alloc("s")));
			
			
		}
		
		return result;
	}

	@Override
	public PathletContainer optional(String label) {
		label = label == null ? "default" : label;
		
		PathletContainer result = (PathletContainer)this.getMember("optional", label);

		// Check the container for an optional member with the given label
		// Create it if it does not exist yet.
		if(result == null) {
			// Return a new relationlet that wraps its effective pattern in an optional block			
			result = new PathletContainer(this, ElementOptional::new);

			BinaryRelation br = new BinaryRelationImpl(new ElementGroup(), Vars.s, Vars.o);
			
			result.add("root", new PathletSimple(
					br.getSourceVar(), br.getTargetVar(),
					new RelationletBinary(br)));
			result.expose("joinSrc", "root", "s");
			result.expose("joinTgt", "root", "o");
			
			keyToAliasToMember.put("optional", label, result);
			RelationletEntry<Pathlet> e = this.add(result);
			String el = e.getId();//this.getLabelForId(e.getId());
			
			
			// Join this pathlet's joinTgt with the joinSrc of the member 
			this.addJoin("root", Arrays.asList(Var.alloc("joinTgt")), el, Arrays.asList(Var.alloc("s")));
			
			//RelationletEntry y;
			
			//this.addJoin(lhsAlias, lhsVars, rhsAlias, rhsVars);
			//this.addJoin("primary", "srcVar", x, result.getSrcVar());
		}
		
		return result;
	}


	@Override
	public Relationlet getMember(String alias) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Var getInternalVar(Var var) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Collection<Var> getExposedVars() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Set<Var> getVarsMentioned() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Set<Var> getFixedVars() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Relationlet setVarFixed(Var var, boolean onOrOff) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Element getElement() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Var getSrcVar() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Var getTgtVar() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Pathlet optional(Pathlet rhs) {
		// TODO Auto-generated method stub
		return null;
	}


//	@Override
//	public PathletContainer optional(String label) {
//		optional(member, label)
//		// TODO Auto-generated method stub
//		return null;
//	}
	
}

class VarRefPathlet
	implements VarRefFn
{
	protected Pathlet pathlet;
	protected boolean isTgtVarMode;
	
	public VarRefPathlet(Pathlet pathlet, boolean isTgtVarMode) {
		super();
		this.pathlet = pathlet;
		this.isTgtVarMode = isTgtVarMode;
	}

//	@Override
//	public Relationlet getRelationlet() {
//		return pathlet;
//	}
	
//	@Override
//	public RelationletEntry getEntry() {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public Var getVar() {
		Var result = isTgtVarMode ? pathlet.getTgtVar() : pathlet.getSrcVar();
		return result;
	}
}


//interface PathletStatic

interface Nodelet2 {
	// Get the joins in which this node participates
	List<Join> getJoins();
}

/**
 * Initially, a relationlet is a column-less table with a single row, into which members can be
 * joined into.
 * 
 * 
 * @author raven
 *
 */
interface RelationletJoin {
	
	/**
	 * Add a relationlet as a member.
	 * Initially, it does not join with anything, hence, it forms a cartesian product with
	 * the prior members.
	 * 
	 * @param alias
	 * @param relation
	 */
	void addMember(String alias, Relationlet relation);
	
	Relationlet getEffectiveRelationlet();
	
	/**
	 * SQL-like join operation
	 * 
	 * RelationJoin j = r.as("a").joinOn(r.getO()).with(otherRel).as("b").on(otherRel.getS());
	 * 
	 * Relation x = r.get("a");
	 */
	
	/**
	 * Return a new identifier for that member
	 * 
	 * 
	 * @param m
	 * @return
	 */
//	String allocateMember(Relationlet m);
//	
//	void addMember(Relationlet m);
//	
//	Relationlet getMember(String label);
//	
//
//	Relationlet union();
}


class MappedList<L, T> {
	protected List<T> items;
	protected Map<L, Integer> labelToIdx = new LinkedHashMap<>();
	protected List<Set<L>> idxToLabels;
	
	
	
	public MappedList(List<T> items, List<Set<L>> idxToLabels) {
		super();
		this.items = items;
		this.labelToIdx = labelToIdx;
		this.idxToLabels = idxToLabels;
	}



//	add(L label, T item) {
//		
//	}
}



// Self-resolving var ref
interface VarRefFn {
	//RelationletEntry getEntry();
	//Relationlet getRelationlet();
	Var getVar();
}

interface VarRefEntry {
	RelationletEntry<?> getEntry();
	Var getVar();
}

class VarRefEntryFnImpl<T extends Relationlet>
	implements VarRefEntry
{
	protected RelationletEntry<T> entry;
	protected Function<? super T, ? extends Var> varFn;
	
	public VarRefEntryFnImpl(RelationletEntry<T> entry, Function<? super T, ? extends Var> varFn) {
		super();
		this.entry = entry;
		this.varFn = varFn;
	}

	@Override
	public RelationletEntry<?> getEntry() {
		return entry;
	}

	@Override
	public Var getVar() {
		T relationlet = entry.getRelationlet();
		Var result = varFn.apply(relationlet);
		return result;
	}
	
}

class VarRefStatic
	implements VarRef
{
	//Relationlet r;
	//protected String label;
	protected List<String> labels;
	protected Var v;
	
	public VarRefStatic(String label, Var v) {
		super();
		this.labels = Collections.singletonList(label);
		this.v = v;
	}

	public VarRefStatic(List<String> labels, Var v) {
		super();
		this.labels = labels;
		this.v = v;
	}

	public List<String> getLabels() {
		return labels;
	}

	public Var getV() {
		return v;
	}
}

class RelationletEntry<T extends Relationlet> {
	protected T relationlet;
	//protected String label; // Allow multiple labels?
	protected String id;

	public RelationletEntry(String id, T relationlet) {
		super();
		this.id = id;
		this.relationlet = relationlet;
		//this.label = label;
	}
	
	/**
	 * Create a var ref to a variable to the relationlet wrapped by this specific entry. 
	 * 
	 * @param var
	 * @return
	 */
	public VarRef createVarRef(Var var) {
		//return new VarRefPathlet(pathlet, isTgtVarMode)
		return null;
	}
	
	public VarRefEntry cerateVarRef(Function<? super T, ? extends Var> varAccessor) {
		return new VarRefEntryFnImpl<T>(this, varAccessor);
	}
	
	
	public T getRelationlet() {
		return relationlet;
	}
//	public String getLabel() {
//		return label;
//	}
	

	// Internal identifier allocated for this entry
	public String getId() {
		return id;
	}

	@Override
	public String toString() {
		return "" + id + ": " + relationlet;
	}
}



class RelationletContainer {
	
}

class RelationletElement
	implements Relationlet
{
	protected Element el;
	protected Set<Var> fixedVars;

	public RelationletElement(Element el) {
		this(el, new LinkedHashSet<>());
	}

	public RelationletElement(Element el, Set<Var> fixedVars) {
		super();
		this.el = el;
		this.fixedVars = fixedVars;
	}

	public Element getEl() {
		return el;
	}

	@Override
	public Relationlet getMember(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Var getInternalVar(Var var) {
		return null;
	}

	@Override
	public Collection<Var> getExposedVars() {
		return null;
	}

	@Override
	public Set<Var> getVarsMentioned() {
		Set<Var> result = ElementUtils.getVarsMentioned(el);
		return result;
	}

	@Override
	public Element getElement() {
		return el;
	}

	@Override
	public Set<Var> getFixedVars() {
		return fixedVars;
	}

	@Override
	public Relationlet setVarFixed(Var var, boolean onOrOff) {
		boolean tmp = onOrOff
			? fixedVars.add(var)
			: fixedVars.remove(var);
			
		
		return this;
		//return result;
	}

	@Override
	public String toString() {
		return getElement() + " (fixed " + getFixedVars() + ")";
	}

	
	@Override
	public RelationletNested materialize() {
		Map<Var, Var> identityMap = getVarsMentioned().stream()
				.collect(CollectorUtils.toLinkedHashMap(x -> x, x -> x));

		RelationletNested result = new RelationletNested(getElement(), identityMap, fixedVars);
		return result;
	}

//	@Override
//	public RelationletNested materialize() {
//		
//		return this;
//	}
}

class Relationlets {
	public static Relationlet from(Element e) {
		return new RelationletElement(e);
	}
}


abstract class PathBuilder {
	public Path optional() {
		return optional("", null);
	}

	public Path optional(Object key) {
		return optional(key, null);
	}

	public Path optional(Object key, String alias) {
		return appendStep(new Step("optional", key, null));
	}
	
	public Path fwd(Resource p) {
		return fwd(p, null);
	}

	public Path fwd(Resource p, String alias) {
		BinaryRelation br = RelationUtils.createRelation(p.asNode(), false);
		return appendStep(new Step("br", br, alias));
		
	}

	public abstract Path appendStep(Step step);
}

class Path
	extends PathBuilder
{
	protected Path parent;
	protected Step step;

	public Path() {
		this(null, null);
	}
	
	public Path(Path parent, Step step) {
		super();
		this.parent = parent;
		this.step = step;
	}
	
	public Path getParent() {
		return parent;
	}

	public Step getStep() {
		return step;
	}

	@Override
	public Path appendStep(Step step) {
		return new Path(this, step);
	}
	
	public static Path newPath() {
		return new Path();
	}
}

class Step {
	protected String type;
	protected Object key;
	protected String alias;

	public Step(String type, Object key, String alias) {
		super();
		this.type = type;
		this.key = key;
		this.alias = alias;
	}
	
	public String getType() {
		return type;
	}

	public Object getKey() {
		return key;
	}
	public String getAlias() {
		return alias;
	}
}

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


class DeepVarRef {
	protected List<String> aliases;
	protected Var var;
}



class NestedVarMap {
	protected Map<Var, Var> localToFinalVarMap;
	protected Map<String, NestedVarMap> memberVarMap;
	protected Set<Var> fixedFinalVars;
	
	
	public NestedVarMap(Map<Var, Var> localToFinalVarMap, Set<Var> fixedFinalVars) {
		this(localToFinalVarMap, fixedFinalVars, Collections.emptyMap());
	}

	public NestedVarMap(Map<Var, Var> localToFinalVarMap, Set<Var> fixedFinalVars, Map<String, NestedVarMap> memberVarMap) {
		super();
		this.localToFinalVarMap = localToFinalVarMap;
		this.memberVarMap = memberVarMap;
		this.fixedFinalVars = fixedFinalVars;
	}
	
	public NestedVarMap(Map<Var, Var> localToFinalVarMap, Map<String, NestedVarMap> memberVarMap,
			Set<Var> fixedFinalVars) {
		super();
		this.localToFinalVarMap = localToFinalVarMap;
		this.memberVarMap = memberVarMap;
		this.fixedFinalVars = fixedFinalVars;
	}

	public NestedVarMap get(List<String> aliases) {
		String alias = aliases.iterator().next();
		
		List<String> sublist = aliases.subList(1, aliases.size() - 1);
		NestedVarMap result = aliases.isEmpty()
				? this
				: memberVarMap.get(alias).get(sublist);
		
		return result;
	}
	
	public Set<Var> getFixedFinalVars() {
		return fixedFinalVars;
	}

	public Map<Var, Var> getLocalToFinalVarMap() {
		return localToFinalVarMap;
	}

	public Map<String, NestedVarMap> getMemberVarMap() {
		return memberVarMap;
	}
	
	public void transformValues(Function<? super Var, ? extends Var> fn) {
		for(Entry<Var, Var> e : localToFinalVarMap.entrySet()) {
			Var before = e.getValue();
			Var after = fn.apply(before);
			e.setValue(after);
		}
		
		for(NestedVarMap child : memberVarMap.values()) {
			child.transformValues(fn);
		}
	}
	
	public NestedVarMap clone() {
		Map<Var, Var> cp1 = new LinkedHashMap<>(localToFinalVarMap);
		Set<Var> cp2 = new LinkedHashSet<>(fixedFinalVars);
		Map<String, NestedVarMap> cp3 = memberVarMap.entrySet().stream()
				.collect(CollectorUtils.toLinkedHashMap(Entry::getKey, e -> e.getValue().clone()));
		
		NestedVarMap result = new NestedVarMap(cp1, cp2, cp3);
		return result;
	}

	@Override
	public String toString() {
		return "NestedVarMap [localToFinalVarMap=" + localToFinalVarMap + ", memberVarMap=" + memberVarMap + "]";
	}
}

class RelationletNested
	extends RelationletElement
//	implements Relationlet
{
	protected NestedVarMap varMap;
	protected Map<String, RelationletNested> aliasToMember;
//	protected Map<Var, Var> exposedVarToElementVar;

	public RelationletNested(
			Element el,
			Map<Var, Var> varMap,
			Set<Var> fixedVars) {
		this(el, new NestedVarMap(varMap, fixedVars), Collections.emptyMap());
	}
	
	public RelationletNested(
			Element el,
			NestedVarMap varMap,
			Map<String, RelationletNested> aliasToMember) {
		super(el); 
		this.varMap = varMap;
		this.aliasToMember = aliasToMember;
//		this.aliasToMember = aliasToMember;
//		this.exposedVarToElementVar = exposedVarToElementVar;
	}
	
	public NestedVarMap getNestedVarMap() {
		return varMap;
	}
//
//	@Override
//	public RelationletNested getMember(String alias) {
//		return null;
//		//return aliasToMember.get(alias);
//	}
//
//	@Override
//	public Var getInternalVar(Var var) {		
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Collection<Var> getExposedVars() {
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
	@Override
	public Set<Var> getFixedVars() {
		return varMap.getFixedFinalVars();
	}
//
	@Override
	public Relationlet setVarFixed(Var var, boolean onOrOff) {
		throw new UnsupportedOperationException("Cannot mark vars as fixed on this object");
	}
}



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

public class AnotherApiTest {
	public static void main(String[] args) {
		RelationletJoinImpl<Relationlet> joiner = new RelationletJoinImpl<>();
		
		
		
		if(false) {
			joiner.add("a", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDF.type.asNode(), Vars.o)).setVarFixed(Vars.s, true));
			joiner.add("b", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDFS.label.asNode(), Vars.o)));
			joiner.add("c", Relationlets.from(ElementUtils.createElementTriple(Vars.s, RDFS.isDefinedBy.asNode(), Vars.y)));
			
			joiner.addJoin("a", Collections.singletonList(Vars.o), "b", Collections.singletonList(Vars.s));
			joiner.addJoin("b", Collections.singletonList(Vars.o), "c", Collections.singletonList(Vars.s));
			
			joiner.materialize();
		}

		if(true) {
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
			
			Path commonParentPath = Path.newPath().optional().fwd(RDF.type);

			Path p1 = commonParentPath.fwd(RDFS.label, "p1");
			Path p2 = commonParentPath.fwd(RDFS.label, "p2");
			
			PathletContainer pathlet = new PathletContainer(null, null);
			pathlet.resolvePath(p1);
			pathlet.resolvePath(p2);
			
			//p1.resolveIn(pathlet);
			
//			pathBuilder.optional()
			
		}
		
	}
}

//interface CompoundStep {
//	
//}



//interface StepVisitor<T> {
//	<T> visit(StepOptional path);
//	
//}


interface StepOptional {
	
}

interface StepUnion {
}


interface StepSimple {
	
}
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
