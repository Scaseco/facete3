package org.aksw.facete.v3.api.path;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementOptional;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public class PathletContainerImpl
	extends RelationletJoinImpl<Pathlet>
	implements PathletContainer
{
	public static final Var srcJoinVar = Var.alloc("srcJoinVar");
	public static final Var tgtJoinVar = Var.alloc("tgtJoinVar");
	
	public static final Pathlet emptyPathlet = newPathlet(BinaryRelationImpl.empty(Vars.s));
	
	public static Pathlet newPathlet(BinaryRelation br) {
		return new PathletSimple(
				br.getSourceVar(), br.getTargetVar(),
				new RelationletBinary(br));
	}
	
//	protected Table<MemberKey, String, PathletMember> keyToAliasToMember;
	protected Table<Object, String, RelationletEntry<? extends Pathlet>> keyToAliasToMember = HashBasedTable.create();
	
	
	//protected PathletContainer container;
	protected Function<Element, Element> elementPostProcessor;
	
	// All element-creating methods connect to this variable
	// protected Var connectorVar;
	// sourceVar / tgtVar...?
	
	protected Resolver resolver;
	
	public PathletContainerImpl(Resolver resolver) {
		this(resolver, emptyPathlet, RelationletJoinImpl::flatten);
	}
	
	public PathletContainerImpl() {
		this(null, emptyPathlet, RelationletJoinImpl::flatten);
	}
	
//	public PathletContainer(Function<? super ElementGroup, ? extends Element> postProcessor) {
//		super(postProcessor);
//		
//		// set up the root member
//		// Note super.add is necessary in order avoid setting up join of the root element with itself
//		super.add("root", new PathletSimple(
//			Vars.s, Vars.s,
//			new RelationletBinary(BinaryRelationImpl.empty(Vars.s))));
//		
//		expose(srcJoinVar.getName(), "root", "s");
//		expose(tgtJoinVar.getName(), "root", "s");
//	}
	public PathletContainerImpl(Resolver resolver, Pathlet rootPathlet, Function<? super ElementGroup, ? extends Element> postProcessor) {
		super(postProcessor);
		
		// set up the root member
		// Note super.add is necessary in order avoid setting up join of the root element with itself
		super.add("root", rootPathlet);
		this.resolver = resolver;
		expose(srcJoinVar.getName(), "root", rootPathlet.getSrcVar().getName());
		expose(tgtJoinVar.getName(), "root", rootPathlet.getTgtVar().getName());
	}

	public RelationletEntry<? extends Pathlet> getRootMember() {
		RelationletEntry<? extends Pathlet> result = this.getMemberByLabel("root");
		return result;
	}

	Supplier<VarRefStatic> resolvePath(Path path) {
		resolvePath(path, true);
		
		Supplier<VarRefStatic> result = () -> pathToVarRef(path);
		
		//PathletContainerImpl result = resolvePath(path, true);
		return result;
	}

	VarRefStatic pathToVarRef(Path path) {
		List<RelationletEntry<PathletContainerImpl>> list = resolvePath(path, false);
		List<String> labels = list.stream()
				.map(RelationletEntry::getId)
				.collect(Collectors.toList());
		Var v = Iterables.getLast(list).getRelationlet().getTgtVar();
		
		VarRefStatic result = new VarRefStatic(labels, v);
		return result;
	}
	
	List<RelationletEntry<PathletContainerImpl>> resolvePath(Path path, boolean createIfNotExists) {
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

		List<RelationletEntry<PathletContainerImpl>> result = resolve(steps.iterator(), createIfNotExists);
		return result;
	}
	
	
	RelationletEntry<PathletContainerImpl> resolveStep(Step step, boolean createIfNotExists) {
		RelationletEntry<PathletContainerImpl> result;
		
		String type = step.getType();
		String alias = step.getAlias();
		Object key = step.getKey();

		switch(type) {
		case "optional":
			result = optional(alias, createIfNotExists);
			break;
		case "br":
			result = step(key, alias, createIfNotExists);
			break;
		default:
			throw new RuntimeException("Unknown step type " + type);
		}
		//optional()
	
		return result;
	}
	

	
	List<RelationletEntry<PathletContainerImpl>> resolve(Iterator<Step> it, boolean createIfNotExists) {
		List<RelationletEntry<PathletContainerImpl>> result = new ArrayList<>();
		
		PathletContainerImpl state = this;
		while(it.hasNext() && state != null) {
			Step step = it.next();
			RelationletEntry<PathletContainerImpl> tmp = state.resolveStep(step, createIfNotExists);
			state = tmp.getRelationlet();
			if(createIfNotExists) {
				Objects.requireNonNull(state, "Step resolution unexpectedly returned null");
			}
			
			result.add(tmp);
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
	
//	Pathlet getMember(Object key, String alias) {
//		Pathlet member = keyToAliasToMember.get(key, alias);
//		//PathletContainer result = member.
//		return member;
//	}

	Pathlet add(Object key, String alias, Pathlet pathlet) {
		return null;
	}

//	public static Node toNode(Object o) {
//		Node result = o instanceof Node
//			? (Node)o
//			: o instanceof RDFNode
//				? ((RDFNode)o).asNode()
//				:null;
//
//		return result;
//	}

//	public PathletContainerImpl step(Object key, String alias) {
//		PathletContainerImpl result = step(true, key, alias);
//		return result;
//	}
	
//	public PathletContainerImpl bwd(Object key, String alias) {
//		PathletContainerImpl result = step(false, key, alias);
//		return result;
//	}

	public RelationletEntry<PathletContainerImpl> step(Object key, String alias, boolean createIfNotExists) {
		P_Path0 p = key instanceof P_Path0 ? (P_Path0)key : null; // Node(key);
		BinaryRelation br;
		Set<Var> fixedVars = Collections.emptySet();
		Resolver subResolver = null;
		if(p == null) {
			br = (BinaryRelation)key;
			subResolver = null;
		} else {
			if(resolver != null) {
				subResolver = resolver.resolve(p, alias);
				Collection<RelationletBinary> brs = subResolver.getPathContrib();
				System.out.println("CONTRIBS:" + brs);
				RelationletBinary rb = brs.iterator().next(); 
				br = rb.getBinaryRelation();
				fixedVars = rb.getFixedVars();
			} else {
				Node n = p.getNode();
				boolean isFwd = p.isForward();
				br = BinaryRelationImpl.create(Vars.s, n, Vars.o, isFwd);
			}
			// TODO Union the relations using (move the method)
			///VirtualPartitionedquery.union()

		}
		
		RelationletEntry<PathletContainerImpl> result = step(createIfNotExists, subResolver, key, br, fixedVars, alias, RelationletJoinImpl::flatten);
		return result;
		
//		//BinaryRelation br = RelationUtils.createRelation(p, false, null)
//		return fwd(null, br, alias);
	}

	@Override
	public RelationletEntry<Pathlet> add(String label, Pathlet item) {
		RelationletEntry<Pathlet> result = super.add(label, item);

		// Join the added pathlet with this container's root
		RelationletEntry<? extends Pathlet> root = getRootMember();
		VarRef rootVarRef = root.createVarRef(x -> x.getTgtVar());
		VarRef memberVarRef = result.createVarRef(x -> x.getSrcVar());
		this.addJoin(rootVarRef, memberVarRef);
		
		return result;
	}

	
//	public PathletContainerImpl step(P_Path0 step, String alias) {
//		Resolver subResolver = resolver.resolve(step, alias);
//
//		Collection<BinaryRelation> brs = subResolver.getPaths();
//		// TODO Union the relations using (move the method)
//		///VirtualPartitionedquery.union()
//
//		BinaryRelation br = brs.iterator().next();
//		Object key = br;
//		PathletContainerImpl result = step(subResolver, true, key, br, alias, RelationletJoinImpl::flatten);
//		return result;
//	}

//	public PathletContainerImpl step(Object key, BinaryRelation br, String alias) {
//		PathletContainerImpl result = step(true, key, br, alias, RelationletJoinImpl::flatten);
//		return result;
//	}

	public RelationletEntry<PathletContainerImpl> step(boolean createIfNotExists, Resolver subResolver, Object key, BinaryRelation br, Collection<Var> fixedVars, String alias, Function<? super ElementGroup, ? extends Element> fn) {
		alias = alias == null ? "default" : alias;

		key = key == null ? "" + br : key;

		//BinaryRelation br = RelationUtils.createRelation(p, false, null);
		// Check if there is a member with this relation pattern already
		RelationletEntry<PathletContainerImpl> result;

		
		result = (RelationletEntry<PathletContainerImpl>)keyToAliasToMember.get(key, alias);//members.find(m -> m.getPattern().equals(br.getElement()));

		
		if(result == null && createIfNotExists) {
			Pathlet childRootPathlet = newPathlet(br);
			childRootPathlet.fixAll(fixedVars);
			PathletContainerImpl subContainer = new PathletContainerImpl(subResolver, childRootPathlet, fn);
//			RelationletBinary r = new RelationletBinary(br);
//			//PathletMember childContainerMember = new PathletMember(childContainer, r, r.getSrcVar(), r.getTgtVar());
//			
//			
//			this.add(br, alias, childContainer);
			
			//result.add(new PathletSimple(br.getSourceVar(), br.getTargetVar(), new RelationletBinary(br)));
			// Set up a join of this node with the newly created member
			//childContainerMember.
//			result.add("root", new PathletSimple(
//					br.getSourceVar(), br.getTargetVar(),
//					new RelationletBinary(br)));
//			result.expose("joinSrc", "root", "s");
//			result.expose("joinTgt", "root", "o");
			
			result = this.add(subContainer);
			keyToAliasToMember.put(key, alias, result);
			//String el = e.getId();//this.getLabelForId(e.getId());
//			VarRef vr = e.createVarRef(Vars.s);
			
			
			// Join this pathlet's joinTgt with the joinSrc of the member
//			VarRef parentVarRef = getRootMember().createVarRef(x -> x.getTgtVar());
//			VarRef childVarRef = e.createVarRef(x -> x.getSrcVar());
//			
//			//this.addJoin("root", Arrays.asList(Var.alloc("o")), el, Arrays.asList(Var.alloc("s")));
//			this.addJoin(parentVarRef, childVarRef);
////			this.addJoin(new VarRefStatic("root", Var.alloc("joinTgt")), new VarRefStatic(Arrays.asList(el, "root"), Vars.s));
			
			
		}
		
		return result;
	}

	//@Override
	public RelationletEntry<PathletContainerImpl> optional(String label, boolean isLookup) {
		
		RelationletEntry<PathletContainerImpl> result = step(isLookup, resolver, "optional", BinaryRelationImpl.empty(), Collections.emptySet(), "default",
				x -> new ElementOptional(RelationletJoinImpl.flatten(x)));
		return result;

		
//		label = label == null ? "default" : label;
//		
//		PathletContainer result = (PathletContainer)this.getMember("optional", label);
//
//		// Check the container for an optional member with the given label
//		// Create it if it does not exist yet.
//		if(result == null) {
//			// Return a new relationlet that wraps its effective pattern in an optional block			
//			result = new PathletContainer(ElementOptional::new);
//
//			BinaryRelation br = new BinaryRelationImpl(new ElementGroup(), Vars.s, Vars.o);
//			
//			result.add("root", new PathletSimple(
//					br.getSourceVar(), br.getTargetVar(),
//					new RelationletBinary(br)));
//			result.expose("joinSrc", "root", "s");
//			result.expose("joinTgt", "root", "o");
//			
//			keyToAliasToMember.put("optional", label, result);
//			RelationletEntry<Pathlet> e = this.add(result);
//			String el = e.getId();//this.getLabelForId(e.getId());
//			
//			
//			// Join this pathlet's joinTgt with the joinSrc of the member 
//			this.addJoin("root", Arrays.asList(Var.alloc("joinTgt")), el, Arrays.asList(Var.alloc("s")));
//			
//			//RelationletEntry y;
//			
//			//this.addJoin(lhsAlias, lhsVars, rhsAlias, rhsVars);
//			//this.addJoin("primary", "srcVar", x, result.getSrcVar());
//		}
//		
//		return result;
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
	public Var getSrcVar() {
		return srcJoinVar;
	}


	@Override
	public Var getTgtVar() {
		return tgtJoinVar;
	}


//	@Override
//	public Pathlet optional(Pathlet rhs, boolean createIfNotExists) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public String toString() {
		return "PathletContainer [keyToAliasToMember=" + keyToAliasToMember + "]";
	}


//	@Override
//	public PathletContainer optional(String label) {
//		optional(member, label)
//		// TODO Auto-generated method stub
//		return null;
//	}
	
}