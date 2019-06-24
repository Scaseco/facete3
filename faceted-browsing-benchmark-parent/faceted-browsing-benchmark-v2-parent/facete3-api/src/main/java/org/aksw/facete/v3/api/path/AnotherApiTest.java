package org.aksw.facete.v3.api.path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.collections.cluster.IndirectEquiMap;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorBlacklist;
import org.aksw.commons.collections.generator.GeneratorFromFunction;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
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




interface Relationlet {
	Relationlet getMember(String alias);
	Var getInternalVar(Var var);

	Collection<Var> getExposedVars();
	Set<Var> getVarsMentioned();
	
	
	default boolean isFixed(Var var) {
		Set<Var> fixedVars = getFixedVars();
		boolean result = fixedVars.contains(var);
		return result;
	}
	
	Set<Var> getFixedVars();
	Relationlet setVarFixed(Var var, boolean onOrOff);
	
	Element getElement();
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

/**
 * A pathlet is a relationlet with designated source and target variables plus
 * operators for concatenation of pathlets
 * 
 * @author raven
 *
 */
interface Pathlet
	extends Relationlet
{
	Var getSrcVar();
	Var getTgtVar();
	
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
class PathletMember
	extends RelationletForwarding
//	implements Pathlet//, Relationlet
{
	public PathletMember(PathletContainer parent, Relationlet relationlet, Var srcVar, Var tgtVar) {
		super();
		this.parent = parent;
		this.relationlet = relationlet;
		this.srcVar = srcVar;
		this.tgtVar = tgtVar;
	}

	protected PathletContainer parent;
	protected Relationlet relationlet;
	
	protected Var srcVar;
	protected Var tgtVar;

	@Override
	protected Relationlet getRelationlet() {
		return relationlet;
	}

//	@Override
//	protected PathletContainer getParent() {
//		return parent;
//	}
//
//	@Override
//	public Var getSrcVar() {
//		return srcVar;
//	}
//
//	@Override
//	public Var getTgtVar() {
//		return tgtVar;
//	}	
}



interface MemberKey {
	
}




class PathletContainer
	extends RelationletJoinImpl
	implements Pathlet
{
	protected Table<MemberKey, String, PathletMember> keyToAliasToMember;
	
	protected PathletContainer container;
	protected Function<Element, Element> elementPostProcessor;
	
	// All element-creating methods connect to this variable
	protected Var connectorVar;
	
	
	// sourceVar / tgtVar...?
	
	public PathletContainer(PathletContainer container, Function<Element, Element> elementPostProcessor) {
		super();
		this.container = container;
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
		}
//		} else {
//			result = this;
//		}
//		
		return result;
	}

	PathletMember add(Pathlet pathlet) {
		return null;
	}
	
	PathletContainer getMember(Object key, String alias) {
		return null;
	}

	PathletMember add(Object key, String alias, Pathlet pathlet) {
		return null;
	}

	PathletContainer fwd(Object key, String alias) {
		return null;
	}
	
	Pathlet fwd(String p, String alias) {
		BinaryRelation br = RelationUtils.createRelation(p, false, null);
		// Check if there is a member with this relation pattern already
		
		
		Pathlet result = getMember(br, alias);//members.find(m -> m.getPattern().equals(br.getElement()));
		
		
		if(result == null) {
			PathletContainer childContainer = new PathletContainer(this, e -> e);
			RelationletBinary r = new RelationletBinary(br);
			//PathletMember childContainerMember = new PathletMember(childContainer, r, r.getSrcVar(), r.getTgtVar());
			
			
			container.add(br, alias, childContainer);
			
			// Set up a join of this node with the newly created member
			//childContainerMember.
			
			
		}
		
		return result;
	}
	
	PathletContainer optional(PathletMember member, String label) {
		PathletContainer result = container.getMember("optional", label);

		// Check the container for an optional member with the given label
		// Create it if it does not exist yet.
		if(result == null) {
			// Return a new relationlet that wraps its effective pattern in an optional block			
			result = new PathletContainer(container, ElementOptional::new);
			
			result.add("root", new RelationletBinary(new BinaryRelationImpl(new ElementGroup(), Vars.s, Vars.o)));
			result.expose("joinSrc", "root", "s");
			result.expose("joinTgt", "root", "o");
			
			PathletMember x = container.add(result);
	
			RelationletEntry y;
			
			//this.addJoin(lhsAlias, lhsVars, rhsAlias, rhsVars);
			//this.addJoin("primary", "srcVar", x, result.getSrcVar());
		}
		
		return null;
		
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


	@Override
	public PathletContainer optional(String label) {
		// TODO Auto-generated method stub
		return null;
	}
	
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

	@Override
	public RelationletEntry getEntry() {
		// TODO Auto-generated method stub
		return null;
	}

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
	
	
	
	public MappedList(List<T> items,  List<Set<L>> idxToLabels) {
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
	RelationletEntry getEntry();
	Var getVar();
}

class VarRefStatic
	implements VarRef
{
	//Relationlet r;
	protected String label;
	protected Var v;
	
	public VarRefStatic(String label, Var v) {
		super();
		this.label = label;
		this.v = v;
	}

	public String getLabel() {
		return label;
	}

	public Var getV() {
		return v;
	}
}

class RelationletEntry {
	protected Relationlet relationlet;
	//protected String label; // Allow multiple labels?
	protected int id;

	public RelationletEntry(int id, Relationlet relationlet) {
		super();
		this.id = id;
		this.relationlet = relationlet;
		//this.label = label;
	}
	
	public Relationlet getRelationlet() {
		return relationlet;
	}
//	public String getLabel() {
//		return label;
//	}
	

	// Internal identifier allocated for this entry
	public int getId() {
		return id;
	}

	@Override
	public String toString() {
		return "" + id + ": " + relationlet;
	}
}



class RelationletContainer {
	
}

class RelationletJoinImpl {

	// TODO better use a TreeMap instead of a separate ridOrder list
	// Then again, with the tree map, we'd have to reassign ids if the order was changed
	Map<Integer, RelationletEntry> ridToEntry = new HashMap<>();
	List<Integer> ridOrder = new ArrayList<>();

	
	//List<RelationletEntry> relationletEntries = new ArrayList<>();
	// TODO In any case, labelToRid should be changed to labelToRe (i.e. make it a reference to the entry object)
	Map<String, Integer> labelToRid = new LinkedHashMap<>();
	
	
	// Exposed vars are seemingly variables of this relationlet and can be accessed without an alias
	// TODO Implement: Variables that are unique to members are implicitly exposed if the exposeUniqueVars flag is true
    // Expose does not imply that the final variable name is fixed.
	Map<Var, VarRefStatic> explicitExposedVars = new LinkedHashMap<>();
	
	
	//Map<String, RelationletEntry> labelToMember = new LinkedHashMap<>();

	Generator<Integer> gen = GeneratorFromFunction.createInt();
	
	// Filter expressions, such as for theta-joins
	List<Expr> exprs;
	
	// Joins
	List<Join> joins = new ArrayList<>();

	public void expose(String exposedName, String alias, String varName) {
		Var exposedVar = Var.alloc(exposedName);
		
		VarRefStatic varRef = new VarRefStatic(alias, Var.alloc(varName));
		explicitExposedVars.put(exposedVar, varRef);
	}
	
	
	// Allocate a new id
	public RelationletEntry add(Relationlet item) {
		String label = "genid" + gen.next();
		RelationletEntry result = add(label, item);
		return result;
	}
	
	public RelationletEntry add(String label, Relationlet item) {
		int id = gen.next();
		RelationletEntry entry = new RelationletEntry(id, item);
		
		//labelToMember.put(label, entry);
		ridToEntry.put(id, entry);
		ridOrder.add(id);
		
		if(label != null) {
			Integer tmp = labelToRid.get(label);
			if(tmp != null) {
				throw new RuntimeException("Label " + label + " already in use");
			}
			
			labelToRid.put(label, id);
		}
		
		return entry;
	}
	
	//Set<VarRef>
	/**
	 * Yield the set of variables that at least two members have in commen
	 * (TODO add condition: and are not being joined on?!)
	 * 
	 */
	public void getConflictingVars() {
		
	}
	
	/**
	 * Yield the set of variables that are unqiue to a single member, hence
	 * a request to it is unambiguous.
	 * 
	 */
	public void getNonConflictingVars() {
		
	}

	// TODO It might be usefule for a relationlet to communicate forbidden var names
	// or suggest a generator for allowed vars. For example, certain prefixes may be forbidden as var names.
//	public Set<Var> getForbiddenVars() {
//		
//	}
	
	public Entry<RelationletEntry, Var> resolveVarRef(Object varRef) {
		// TODO We way want to use a tag interface for var-refs + possibly visitor pattern here
		Entry<RelationletEntry, Var> result;

		if(varRef instanceof VarRefStatic) {
			VarRefStatic vr = (VarRefStatic)varRef;
			String alias = vr.getLabel();
			Var v = vr.getV();
			
			RelationletEntry entry = ridToEntry.get(labelToRid.get(alias));
			result = Maps.immutableEntry(entry, v);
			
		} else if(varRef instanceof VarRefFn) {
			VarRefFn vr = (VarRefFn)varRef;
			result = Maps.immutableEntry(vr.getEntry(), vr.getVar());
		} else {
			throw new IllegalArgumentException("Unsupported var ref type: " + varRef);
		}
		
		return result;
	}
	
	public static List<VarRef> toVarRefs(String alias, List<Var> vars) {
		List<VarRef> result = vars.stream().map(v -> (VarRef)new VarRefStatic(alias, v)).collect(Collectors.toList());
		return result;
	}
	
	public void addJoin(VarRef lhsVarRef, VarRef rhsVarRef) {
		Join join = new Join(Collections.singletonList(lhsVarRef), Collections.singletonList(rhsVarRef));
		joins.add(join);
	}
	
	public void addJoin(String lhsAlias, List<Var> lhsVars, String rhsAlias, List<Var> rhsVars) {
		//labelToMember.get(lhsAlias);
		List<VarRef> lhs = toVarRefs(lhsAlias, lhsVars);
		List<VarRef> rhs = toVarRefs(rhsAlias, rhsVars);

		Join join = new Join(lhs, rhs);
		joins.add(join);
	}
	
	public Relation effective() {
		return null;
	}
	
	
	public Relationlet getMemberByLabel(String label) {
		return ridToEntry.get(labelToRid.get(label)).getRelationlet();
	}
	
	public Iterable<RelationletEntry> getRelationletEntries() {
		return () -> ridOrder.stream().map(ridToEntry::get).iterator();
	}
	/**
	 * Create a snapshot of any referenced relationlet
	 * 
	 */
	MappedElement materialize() {
		//Set<Var> forbiddenVars = new HashSet<>();
		Predicate<Var> baseBlacklist = x -> false;

//		List<?> members = new ArrayList<>();
//		List<Map<Var, Var>> memberVarMaps = members.stream()
//				.map(x -> new LinkedHashMap<Var, Var>())
//				.collect(Collectors.toList());
		
		
		IndirectEquiMap<Entry<Integer, Var>, Var> aliasedVarToEffectiveVar = new IndirectEquiMap<>();

		for(Join join : joins) {
//			RelationletEntry lhsEntry = ridToEntry.get(labelToRid.get(join.getLhsAlias()));
//			RelationletEntry rhsEntry = ridToEntry.get(labelToRid.get(join.getRhsAlias()));
//			List<Var> lhsVars = join.getLhsVars();
//			List<Var> rhsVars = join.getRhsVars();

//			int lhsId = lhsEntry.getId();
//			int rhsId = rhsEntry.getId();
			
//			Relationlet lhsRel = lhsEntry.getRelationlet();
//			Relationlet rhsRel = rhsEntry.getRelationlet();
//			
//			Map<Var, Var> lhsVarMap;
//			Map<Var, Var> rhsVarMap;
			

			List<VarRef> lhsRefs = join.getLhs();
			List<VarRef> rhsRefs = join.getRhs();

			
			//int n = lhsVars.size();
			int n = join.getLhs().size();
			// TODO Assert that var lists sizes are equal
			
			for(int i = 0; i < n; ++i) {
				VarRef lhsRef = lhsRefs.get(i);
				VarRef rhsRef = rhsRefs.get(i);

				Entry<RelationletEntry, Var> lhsEntry = resolveVarRef(lhsRef);
				Entry<RelationletEntry, Var> rhsEntry = resolveVarRef(rhsRef);

				
				int lhsId = lhsEntry.getKey().getId();
				int rhsId = rhsEntry.getKey().getId();
				
				Var lhsVar = lhsEntry.getValue();
				Var rhsVar = rhsEntry.getValue();

				
//				Var lhsVar = lhsVars.get(i);
//				Var rhsVar = rhsVars.get(i);
				
				Entry<Integer, Var> lhsE = Maps.immutableEntry(lhsId, lhsVar);
				Entry<Integer, Var> rhsE = Maps.immutableEntry(rhsId, rhsVar);
				
				// Put the rhs var first, so it gets renamed first
				aliasedVarToEffectiveVar.stateEqual(rhsE, lhsE);
			}
		}
		
		
		// Now that we have clustered the join variables, allocate for each cluster an
		// effective variable
		// In order to tidy up the output, we sort clusters that only make use of the same variable first
		// If multiple clusters only make use of the same variable, they are ordered by size and var name
		
		Map<Integer, Collection<Entry<Integer, Var>>> rawClusters = aliasedVarToEffectiveVar.getEquivalences().asMap();

		Map<Integer, Collection<Entry<RelationletEntry, Var>>> clusters = rawClusters.entrySet().stream()
				.collect(Collectors.toMap(
						Entry::getKey,
						e -> e.getValue().stream()
							.map(f -> Maps.immutableEntry(ridToEntry.get(f.getKey()), f.getValue()))
							.collect(Collectors.toList())
						));
		
		// Index of which relationlets mention a var
		// Used to check whether a cluster covers all mentions of a var - in this case,
		// no renaming has to be performed
		Multimap<Var, Integer> varToRids = HashMultimap.create();
		//Multimap<Integer, Var> ridToVars = LinkedHashMultimap.create();

		
		for(RelationletEntry e : getRelationletEntries()) {
			int id = e.getId();
			Relationlet r = e.getRelationlet();
			Set<Var> varsMentioned = r.getVarsMentioned();
			
			for(Var v : varsMentioned) {
				varToRids.put(v, id);
				
				//ridToVars.put(id, v);
			}
		}
		
		// The same var can join in different clusters with different relationlets
//		Multimap<Var, Integer> joinVarToElRids = HashMultimap.create();
//		for(Entry<Var, Integer> e : aliasedVarToEffectiveVar.getEquivalences()) {
//			joinVarToElRids.put(e.getKey(), oo);
//		}
		
		Map<Integer, Set<Var>> clusterToVars = clusters.entrySet().stream()
				.collect(Collectors.toMap(
						Entry::getKey,
						e -> e.getValue().stream()
							.map(Entry::getValue)
							//.map(Var::getName)
							.collect(Collectors.toCollection(HashSet::new))));
		 
		
		Collection<Var> mentionedVars = Collections.emptySet();
		Predicate<Var> isBlacklisted = baseBlacklist.and(mentionedVars::contains);
		
		//GeneratorLending<Var>
		Generator<Var> basegen = VarGeneratorImpl2.create();
		Generator<Var> vargen = GeneratorBlacklist.create(basegen, isBlacklisted);

		Set<Var> takenFinalVars = new HashSet<>();
		
		Table<Integer, Var, Var> ridToVarToFinalVal = HashBasedTable.create(); 
		for(Entry<Integer, Collection<Entry<RelationletEntry, Var>>> e : clusters.entrySet()) {
			int clusterId = e.getKey();
			Collection<Entry<RelationletEntry, Var>> members = e.getValue();
			
			Set<Var> fixedVars = new LinkedHashSet<>();
			Multimap<Var, RelationletEntry> varToRe = ArrayListMultimap.create();
			for(Entry<RelationletEntry, Var> f : members) {
				RelationletEntry re = f.getKey();
				Relationlet r = re.getRelationlet();
				Var v = f.getValue();
				
				varToRe.put(v, re);
				boolean isFixed = r.isFixed(v);
				if(isFixed) {
					fixedVars.add(v);
				}
			}
			
			if(fixedVars.size() > 1) {
				Multimap<Var, RelationletEntry> conflictMentions = Multimaps.filterKeys(varToRe, fixedVars::contains);
				throw new RuntimeException("Conflicting fixed vars encountered when processing join: " + fixedVars + " with mentions in " + conflictMentions);
			}

			// If there is a fixed variable within the set of used vars, pick this one
			// If there are multiple ones, we have a conflict and throw an exception
			// TODO Such conflicts should be detected early when adding the join condition!
			
			

			Var finalVar = null;
			boolean isFinalVarFixed = false;

			if(fixedVars.size() == 1) {
				finalVar = fixedVars.iterator().next();
				isFinalVarFixed = true;
			}
			
			
			if(finalVar == null) {
				// If any of the variables is not taken yet, use this for the cluster otherwise
				// allocate a fresh variable
				Set<Var> usedVars = varToRe.keySet();
	
				for(Var var : usedVars) {
					if(!takenFinalVars.contains(var)) {
						finalVar = var;
						break;
					}
				}
			}

			if(finalVar == null) {
				finalVar = vargen.next();
			}

			takenFinalVars.add(finalVar);

			for(Entry<RelationletEntry, Var> ridvar : clusters.get(clusterId)) {
				int rid = ridvar.getKey().getId();
				Var var = ridvar.getValue();
				ridToVarToFinalVal.put(rid, var, finalVar);
				
				// Remove the entry from the conflicts
				varToRids.remove(var, rid);
				//ridToVars.remove(rid, var);
			}
		}
		
		// Make all remaining variables distinct from each other
//		for(Integer rid : ridOrder) {
//			ridToVars
//		}
		
		for(Entry<Var, Collection<Integer>> e : varToRids.asMap().entrySet()) {
			Var var = e.getKey();
			Collection<Integer> tmpRids = e.getValue();
			
			// Sort the rids according to the ridOrder
			List<Integer> rids = new ArrayList<>(ridOrder);
			rids.retainAll(tmpRids);
			
			for(Integer rid : rids) {
				boolean isFixed = ridToEntry.get(rid).getRelationlet().isFixed(var);
				if(isFixed) {
					takenFinalVars.add(var);
					ridToVarToFinalVal.put(rid, var, var);
				}
			}			

			for(Integer rid : rids) {
				boolean isFixed = ridToEntry.get(rid).getRelationlet().isFixed(var);
				if(!isFixed) {
					boolean isTaken = takenFinalVars.contains(var);
					
					Var finalVar = isTaken ? vargen.next() : var;
					takenFinalVars.add(finalVar);
					
					ridToVarToFinalVal.put(rid, var, finalVar);
				}
			}
		}
		
//		Multimap<Var, Entry<Integer, Var>> finalVarToRidVars;
		
		
		ElementGroup group = new ElementGroup();
		for(RelationletEntry re : getRelationletEntries()) {
			int rid = re.getId();
			Element el = re.getRelationlet().getElement();
			
			Map<Var, Var> originToFinal = ridToVarToFinalVal.row(rid);
			Element contrib = ElementUtils.applyNodeTransform(el, new NodeTransformSubst(originToFinal));
			group.addElement(contrib);
			
		}
		
		
		Map<Var, Var> resolvedExposedVar = new LinkedHashMap<>();
		for(Entry<Var, VarRefStatic> eve : explicitExposedVars.entrySet()) {
			Var key = eve.getKey();
			VarRefStatic vr = eve.getValue();
			String label = vr.getLabel();
			Var refVar = vr.getV();
			int rid = labelToRid.get(label);
			Var finalVar = ridToVarToFinalVal.get(rid, refVar);
			resolvedExposedVar.put(key, finalVar);
		}
		
		MappedElement result = new MappedElement(group, resolvedExposedVar);
		
		System.out.println(ridToVarToFinalVal);
		System.out.println(group);
		return result;
	}
	
	/**
	 * Flatten a var name by giving it a new name that is visible
	 * directly at this relationlet
	 * 
	 * @param name
	 * @param alias
	 * @param var
	 * @return
	 */
//	public Var expose(Var name, String alias, Var var) {
//		
//	}
	
	/**
	 * If var is unique among all member relationlets (up to the point of reference)
	 * expose its occurrence. If the var is ambiguous, raise an exception.
	 * 
	 * @param var
	 */
//	public expose(Var var) {
//		
//	}
}


class RelationletElement
	implements Relationlet
{
	protected Element el;
	protected Set<Var> fixedVars = new LinkedHashSet<>();

	public RelationletElement(Element el) {
		super();
		this.el = el;
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
		return el + " (fixed " + fixedVars + ")";
	}
	
	
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

class MappedElement {
	protected Element element;
	//protected Relation relation;
	protected Map<Var, Var> exposedVarToElementVar;
	
	public MappedElement(Element element, Map<Var, Var> exposedVarToElementVar) {
		super();
		this.element = element;
		this.exposedVarToElementVar = exposedVarToElementVar;
	}

	public Element getElement() {
		return element;
	}

	public Map<Var, Var> getExposedVarToElementVar() {
		return exposedVarToElementVar;
	}
}

public class AnotherApiTest {
	public static void main(String[] args) {
		RelationletJoinImpl joiner = new RelationletJoinImpl();
		
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
			MappedElement me = joiner.materialize();
//			System.out.println("finalVar: "  + me.getExposedVarToElementVar().get(Var.alloc("foo")));
			System.out.println("finalVar: "  + me.getExposedVarToElementVar());
			
		}

		
		if(false) {
			
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
