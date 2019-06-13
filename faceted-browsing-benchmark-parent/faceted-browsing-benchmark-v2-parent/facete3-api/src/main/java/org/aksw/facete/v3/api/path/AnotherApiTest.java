package org.aksw.facete.v3.api.path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.aksw.commons.collections.cluster.IndirectEquiMap;
import org.aksw.commons.collections.generator.Generator;
import org.aksw.commons.collections.generator.GeneratorBlacklist;
import org.aksw.commons.collections.generator.GeneratorFromFunction;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.VarGeneratorImpl2;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.syntaxtransform.NodeTransformSubst;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedHashMultimap;
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
	public Join(String lhsAlias, List<Var> lhsVars, String rhsAlias, List<Var> rhsVars) {
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

class VarRef {
	Relationlet r;
	Var v;
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


class RelationletJoinImpl {
	
	Map<Integer, RelationletEntry> ridToEntry = new HashMap<>();
	List<Integer> ridOrder = new ArrayList<>();
	
	//List<RelationletEntry> relationletEntries = new ArrayList<>();
	Map<String, Integer> labelToRid = new LinkedHashMap<>();
	//Map<String, RelationletEntry> labelToMember = new LinkedHashMap<>();

	Generator<Integer> gen = GeneratorFromFunction.createInt();
	
	// Filter expressions, such as for theta-joins
	List<Expr> exprs;
	
	// Joins
	List<Join> joins = new ArrayList<>();

	
	public void add(String label, Relationlet item) {
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
	
	public void addJoin(String lhsAlias, List<Var> lhsVars, String rhsAlias, List<Var> rhsVars) {
		//labelToMember.get(lhsAlias);
		Join join = new Join(lhsAlias, lhsVars, rhsAlias, rhsVars);
		joins.add(join);
	}
	
	public Relation effective() {
		return null;
	}
	
	
	public Iterable<RelationletEntry> getRelationletEntries() {
		return () -> ridOrder.stream().map(ridToEntry::get).iterator();
	}
	/**
	 * Create a snapshot of any referenced relationlet
	 * 
	 */
	void materialize() {
		//Set<Var> forbiddenVars = new HashSet<>();
		Predicate<Var> baseBlacklist = x -> false;

//		List<?> members = new ArrayList<>();
//		List<Map<Var, Var>> memberVarMaps = members.stream()
//				.map(x -> new LinkedHashMap<Var, Var>())
//				.collect(Collectors.toList());
		
		
		IndirectEquiMap<Entry<Integer, Var>, Var> aliasedVarToEffectiveVar = new IndirectEquiMap<>();

		for(Join join : joins) {
			RelationletEntry lhsEntry = ridToEntry.get(labelToRid.get(join.getLhsAlias()));
			RelationletEntry rhsEntry = ridToEntry.get(labelToRid.get(join.getRhsAlias()));
			
			int lhsId = lhsEntry.getId();
			int rhsId = rhsEntry.getId();
			
//			Relationlet lhsRel = lhsEntry.getRelationlet();
//			Relationlet rhsRel = rhsEntry.getRelationlet();
//			
//			Map<Var, Var> lhsVarMap;
//			Map<Var, Var> rhsVarMap;
			
			
			List<Var> lhsVars = join.getLhsVars();
			List<Var> rhsVars = join.getRhsVars();
			
			int n = lhsVars.size();
			// TODO Assert that var lists sizes are equal
			
			for(int i = 0; i < n; ++i) {
				Var lhsVar = lhsVars.get(i);
				Var rhsVar = rhsVars.get(i);
				
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
		
		
		System.out.println(ridToVarToFinalVal);
		System.out.println(group);
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


interface Pathlet2 {
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
	Pathlet2 optional(Pathlet2 rhs);
	
	// get or create an optional block with the given label
	Pathlet2 optional(String label);
	
	// get or create an optional block with a null label
    default Pathlet2 optional() {
    	return optional((String)null);
    }
	
    static Pathlet2 as(String alias) {
	    return null;
    }
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
			
			joiner.materialize();
		}

	}
}



