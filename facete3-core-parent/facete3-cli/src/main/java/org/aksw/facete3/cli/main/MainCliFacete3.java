package org.aksw.facete3.cli.main;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetNodeResource;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.FacetedQueryResource;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.impl.BgpNodeUtils;
import org.aksw.facete.v3.impl.FacetNodeImpl;
import org.aksw.facete.v3.impl.FacetValueCountImpl_;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.impl.HLFacetConstraintImpl;
import org.aksw.facete3.cli.main.GridLayout2.Alignment;
import org.aksw.jena_sparql_api.algebra.expr.transform.ExprTransformVirtualBnodeUris;
import org.aksw.jena_sparql_api.algebra.utils.VirtualPartitionedQuery;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.RelationImpl;
import org.aksw.jena_sparql_api.concepts.TernaryRelation;
import org.aksw.jena_sparql_api.concepts.TernaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.RDFConnectionFactoryEx;
import org.aksw.jena_sparql_api.data_query.api.DataQuery;
import org.aksw.jena_sparql_api.data_query.impl.NodePathletPath;
import org.aksw.jena_sparql_api.data_query.util.KeywordSearchUtils;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.pathlet.Path;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rx.RDFDataMgrEx;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.stmt.SparqlQueryParserImpl;
import org.aksw.jena_sparql_api.user_defined_function.UserDefinedFunctions;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.PrefixUtils;
import org.aksw.jena_sparql_api.utils.QueryUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.Directed;
import org.apache.jena.JenaRuntime;
import org.apache.jena.ext.com.google.common.base.Strings;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.graph.Traverser;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.rdfconnection.RDFConnectionRemote;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WebContent;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Transform;
import org.apache.jena.sparql.algebra.TransformGraphRename;
import org.apache.jena.sparql.algebra.Transformer;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.user.UserDefinedFunctionDefinition;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Borders.StandardBorder;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;


//If we wanted to create an Amazon like faceted interface we'd need:
//Ranking function over a set of values - rank(concept) -> BinaryRelation (resource, rank-value)
//Rank is actually just a 'combine'* with a ranking attribute
//We can then sort the binary relation by rank
//binaryRelation.clearOrder(); binaryRelation.addOrder(binaryRelation.getTargetVar(), 1)
//* my current term for the generalization of a sparql join on the syntax level - which isn't necessarily a join in the first place

/**
 * 
 * [Show: Datasets / Distributions] [View: Table / Tree]
 * [Facets]
 * 
 * 
 * @author Claus Stadler, Nov 24, 2018
 *
 */
public class MainCliFacete3 {
	private static final Logger logger = LoggerFactory.getLogger(MainCliFacete3.class);
	
	public static final char CHAR_UPWARDS_ARROW = '\u2191';
	public static final char CHAR_DOWNWARDS_ARROW = '\u2193';
	
	public static final String[] sortModeLabel = { "A-Z", "0-9" };

	// Mapping of ui sort directions to ui labels
	public static final String[] sortDirLabel = { Character.toString(CHAR_DOWNWARDS_ARROW), Character.toString(CHAR_UPWARDS_ARROW) };

	// Mapping of ui sort directions to jena query sort directions
	public static final int[] sortDirMapJena = { Query.ORDER_DESCENDING, Query.ORDER_ASCENDING };


	protected PrefixMapping globalPrefixes = new PrefixMappingImpl();
	//.loadModel("rdf-prefixes/prefix.cc.2019-12-17.jsonld");
	
	// Normalize a short form of select sparql queries, where SELECT may be omitted
	public static String injectSelect(String queryStr) {
		// TODO Implement
		// If the query does not parse as sparql, attempt to inject a SELECT before
		// the first occurrence of a '(' or '?'
		// i.e. SELECT ?foo and SELECT (?foo AS ?bar)
		// Alternatively, after the PREFIX block:
		// (PREFIX foo:<> .)
		return queryStr;
	}
	
	public static UnaryRelation parseConcept(String queryStr, PrefixMapping prefixes) {
		Query query = SparqlQueryParserImpl.create(prefixes)
				.apply(queryStr);
		
		if(!query.isSelectType()) {
			throw new RuntimeException("Select type expected, got " + queryStr);
		}
		
		List<String> resultVars = query.getResultVars();
		if(resultVars.size() != 1) {
			throw new RuntimeException("Exactly one result var expected, got " + queryStr);
		}
		
		Element el = query.getQueryPattern();
		Var v = Var.alloc(resultVars.get(0));
		
		UnaryRelation result = new Concept(el, v);
		return result;
	}
	
	
	public static void setAttr(String clazzName, Object obj, String fieldName, Object value) {
		try {
			Class<?> clazz = Class.forName(clazzName);
			setAttr(clazz, obj, fieldName, value);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void setAttr(Class<?> clazz, Object obj, String fieldName, Object value) {
		try {
			Field field = clazz.getField(fieldName);
			field.setAccessible(true);
			field.set(obj, value);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static BgpNode HACK = ModelFactory.createDefaultModel().createResource("should not appear anywhere").as(BgpNode.class);

	public static <T> T getCell(TableModel<T> model, int x, int y) {
		int rowCount = model.getRowCount();
		int colCount = model.getColumnCount();
		
		T result = x >= 0 && x < rowCount && y >= 0 && y < colCount
			? model.getCell(y, x)
			: null;
		
//		Node node = x >= 0 && y >= 0 ? resourceTable.getTableModel().getCell(x, y) : null;

		return result;
	}
	
	
	public HLFacetConstraint<?> toHlConstraint(FacetedQuery fq, FacetConstraint fc) {
//		PathAccessor<BgpNode> pathAccessor = new PathAccessorImpl(fq.root().as(FacetNodeResource.class).state());
//		Map<Node, BgpNode> map = PathAccessorImpl.getPathsMentioned(expr, pathAccessor::tryMapToPath);

		FacetedQueryResource r = fq.as(FacetedQueryResource.class);
		
		
		// HACK FacetNodeImpl requires a bgpNode - but we don't need its value
		// We only need it in order to set up HLFacetConstraint.pathsMentioned
		FacetNode tmp = new FacetNodeImpl(r, HACK);
		
		HLFacetConstraint<?> result = new HLFacetConstraintImpl<Void>(null, tmp, fc);
		return result;
	}
	
	public void updateResourceView(RDFNode n) {
		TableModel<RDFNode> model = resourceTable.getTableModel();
		resourceTable.setSelectedColumn(-1);
		resourceTable.setSelectedRow(0);
		model.clear();
		
		if(n == null) {
			resourceTableSubject = null;
			resourceSubjectLabel.setText("No resource selected");
		} else {
			resourceSubjectLabel.setText("s: " + n);
			
			if(n.isResource()) {
				Resource r = n.asResource();
	
				List<Property> predicates = r.listProperties().mapWith(Statement::getPredicate).toList()
						.stream().distinct().collect(Collectors.toList());
				
				// TODO Fetch all nodes and resolve their labels
				
				Property prev = null;
				for(Property curr : predicates) {
					Property p = null;
					if(prev != curr) {
						p = curr;
						prev = curr;
					}
	
					for(RDFNode rdfNode : ResourceUtils.listPropertyValues(r, curr).toList()) {
						//Node o = rdfNode.asNode();
						model.addRow(p, rdfNode);
					}
				}
			}
		}
		
		//resourceTable.invalidate();

//		System.out.println("resourcelabel prefsize: " + resourceSubjectLabel.getPreferredSize());
//		System.out.println("resourcelabel prefsize: " + resourceSubjectLabel.getSize());
//		System.out.println("resourcepanel Prefsize: " + resourcePanel.getPreferredSize());
//		System.out.println("resourcepanel Actualsize: " + resourcePanel.getSize());
//		System.out.println("resourcetable Prefsize: " + resourceTable.getPreferredSize());
//		System.out.println("resourcetable Actualsize: " + resourceTable.getSize());


	}
	
	/**
	 * Extract all nodes involved with the constraint so they can be resolved to labels
	 * 
	 * @param constraint
	 * @return
	 */
	public static Set<Node> extractNodes(HLFacetConstraint<?> constraint) {
		
		Map<Node, FacetNode> map = constraint.mentionedFacetNodes();

		Set<Node> nodes = new LinkedHashSet<>();
		
		for(FacetNode fn : map.values()) {
			BgpNode state = fn.as(FacetNodeResource.class).state();
			SimplePath simplePath = BgpNodeUtils.toSimplePath(state);
			Set<Node> contrib = SimplePath.mentionedNodes(simplePath);
			
			nodes.addAll(contrib);
		}
		
		// Add all iri and literal constants of the expr to the result
		Expr expr = constraint.expr();
		Set<Node> contrib = Streams.stream(Traverser.<Expr>forTree(e -> e.isFunction() ? e.getFunction().getArgs() : Collections.emptyList())
			.depthFirstPreOrder(expr))
			.filter(Expr::isConstant)
			.map(Expr::getConstant)
			.map(NodeValue::asNode)
			.filter(n -> n.isURI() || n.isLiteral())
			.collect(Collectors.toSet());
			
		nodes.addAll(contrib);
		
		return nodes;
	}
	
	public static Map<Node, SimplePath> indexPaths(HLFacetConstraint<?> constraint) {
		Map<Node, SimplePath> result = constraint.mentionedFacetNodes().entrySet().stream()
				.map(e -> {
					Node k = e.getKey();
					FacetNode fn = e.getValue();
					BgpNode state = fn.as(FacetNodeResource.class).state();
					SimplePath simplePath = BgpNodeUtils.toSimplePath(state);
					return Maps.immutableEntry(k, simplePath);
				})
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		return result;
	}
	
	public static String toString(P_Path0 step, Function<Node, String> nodeToStr) {
		Node node = step.getNode();
		String result = (!step.isForward() ? "^" : "") + nodeToStr.apply(node);
		return result;
	}

	public static String toString(SimplePath sp, Function<Node, String> nodeToStr) {
		String result = sp.getSteps().stream()
			.map(step -> toString(step, nodeToStr))
			.collect(Collectors.joining("/"));
		
		return result;
	}

	public static String toString(
			HLFacetConstraint<?> constraint,
			LookupService<Node, String> labelService,
			PrefixMapping prefixes) {
		Expr expr = constraint.expr();
		Set<Node> nodes = extractNodes(constraint);
		
		Map<Node, String> nodeToLabel = getLabels(nodes, Function.identity(), labelService, prefixes);
		
		Map<Node, String> bgpNodeLabels = indexPaths(constraint).entrySet().stream()
			.collect(Collectors.toMap(Entry::getKey, e -> toString(e.getValue(), nodeToLabel::get)));
		
		// Combine the maps to get the final label mapping
		nodeToLabel.putAll(bgpNodeLabels);
		
		String result = toString(expr, nodeToLabel::get);
		
		return result;
	}
	
	
	public static String toString(Expr expr, Function<? super Node, ? extends String> nodeToLabel) {
		String result;
		if(expr.isConstant()) {
			Node node = expr.getConstant().asNode();
			result = nodeToLabel.apply(node);
		} else if(expr.isVariable()) {
			Node node = expr.asVar();
			result = nodeToLabel.apply(node);
		} else {
			ExprFunction f = expr.getFunction();
			String symbol = f.getFunctionSymbol().getSymbol();
			if(symbol == null || symbol.isEmpty()) {
				symbol = f.getFunctionIRI();
			}
			
			List<String> argStrs = f.getArgs().stream()
					.map(e -> toString(e, nodeToLabel))
					.collect(Collectors.toList());
			
			result =
					argStrs.size() == 1 ? symbol + argStrs.iterator().next() :
					argStrs.size() == 2 ? argStrs.get(0) + " " + symbol + " " + argStrs.get(1) :
					symbol + "(" + Joiner.on(",").join(argStrs) + ")";
		}
		
		return result;
	}
	
	
	//KeyStroke resourceViewKey = new KeyStroke(' ', false, false);
	public static final char resourceViewKey = ' ';
	public static final char showQueryKey = 's';
	
	//RDFConnection conn;
	
	//RDFConnection conn;
	FacetedQuery fq;
	LookupService<Node, String> labelService;

	ActionListBox facetList = new ActionListBox(); //new TerminalSize(30, 10));
	CheckBoxList<FacetValueCount> facetValueList = new CheckBoxList<>();
	Table<RDFNode> resultTable = new Table<>("Item");
	Table<RDFNode> cartTable = new Table<>("Item");

	CheckBoxList<HLFacetConstraint<?>> constraintList = new CheckBoxList<>();
	Panel facetPathPanel = new Panel();
	StandardBorder resourcePanelBorder;
	StandardBorder resultPanelBorder;
	StandardBorder facetValuePanelBorder;

	
	String facetFilter = null;
	
	String facetValueFilter = null;
	
	FacetDirNode fdn;
	Node selectedFacet = null;
	
	boolean includeAbsent = false; //true;
	
	
	int facetSortMode = 1; // 0 = lexicographic, 1 = by count
	int facetSortDir = 0;

	int facetValueSortMode = 1; // 0 = lexicographic, 1 = by count
	int facetValueSortDir = 0;

	
	
	Node resourceTableSubject = null;
	Label2 resourceSubjectLabel = new Label2("");
	Table<RDFNode> resourceTable = new Table<RDFNode>("p", "o");
	
	
	Panel resourcePanel = new Panel();
	Panel itemPagePanel = new Panel();


	Panel facetValuePagePanel = new Panel();

	
	class Test<P> {
		P value;
		public Test(P value) {
			this.value = value;
		}
		
	}

	public void test() {
		Collection<Test<?>> c = new ArrayList<>();
		c.add(new Test<String>("hi"));
		c.add(new Test<Number>(1));
		
	}
	
	public void updateConstraints(FacetedQuery fq) {
		constraintList.clearItems();
		//constraintList.addl
		for(FacetConstraint c : fq.constraints()) {
			HLFacetConstraint<?> hlc = toHlConstraint(fq, c);
			//String str = toString(hlc, labelService);
			
			// TODO We should add pairs with the facet constraints together with the precomputed string
			// then we can batch the label lookups here
			constraintList.addItem(hlc, true);
		}
	}
	
	
	public DataQuery<RDFNode> itemsDataQuery(FacetedQuery fq) {
		DataQuery<RDFNode> result = fq.root().availableValues();
		return result;
	}
	
	
	long itemPage[] = {0};
	public void updateItems(FacetedQuery fq) {
		Stopwatch sw = Stopwatch.createStarted();
		
		Long count = fq.focus().availableValues().count().blockingGet().getCount();
		
		// Update paginator with item count
		int itemsPerPage = 20;
		
		
		Paginator<Page> paginator = new PaginatorImpl(itemsPerPage);
		List<Page> pages = paginator.createPages(count, itemPage[0]);

		
		// Update the paginator buttons. If one has a focus, we memorize it so
		// we can restore it afterwards.
		boolean refocusPage = itemPagePanel.getChildren().stream()
				.anyMatch(child -> child instanceof Interactable && ((Interactable)child).isFocused());
		
		itemPagePanel.removeAllComponents();
		for(Page page : pages) {
//			if(page.isActive()) {
//				itemPagePanel.addComponent(new Label2("" + page.getPageNumber()));
//			} else {
				Button btn = new Button("" + page.getPageNumber(), () -> {
					itemPage[0] = page.getPageOffset();
					updateItems(fq);
				});
				itemPagePanel.addComponent(btn);
				
				if(page.isActive() && refocusPage) {
					btn.takeFocus();
				}
//			}
		}

		
		//setAttr("com.googlecode.lanterna.gui2.Borders$StandardBorder", resultPanelBorder, "title", "" + count);
		//System.out.println("Item count: " + count);
		long itemStart = itemPage[0];
		long itemEnd = Math.min(itemStart + itemsPerPage, count);
		String title = count == 0
				? "(no matches)"
				: String.format("Matches %d-%d of %d", itemStart + 1, itemEnd, count);
		
		resultPanelBorder.setTitle(title);
		
		List<RDFNode> items = itemsDataQuery(fq) //fq.root().availableValues()
				.offset(itemStart)
				.limit(itemsPerPage)
				.exec().toList().blockingGet();

		MainCliFacete3.<RDFNode>enrichWithLabels(items, RDFNode::asNode, labelService, globalPrefixes);

		
//		Map<Node, String> labelMap = getLabels(nodes, Function.identity(), labelService);

		
		TableModel<RDFNode> model = resultTable.getTableModel();
		model.clear();
		
		for(RDFNode item : items) {
			model.addRow(item);
		}
		
		if(resultTable.getSelectedRow() > model.getRowCount()) {
			resultTable.setSelectedRow(model.getRowCount() - 1);
		}
		
		if(resultTable.getSelectedColumn() > model.getColumnCount()) {
			resultTable.setSelectedColumn(model.getColumnCount() - 1);
		}
		
		logger.info("updateItems: " + sw.elapsed(TimeUnit.MILLISECONDS) / 1000.0 + "s");
//		System.out.println("resulttable Prefsize: " + resultTable.getPreferredSize());
//		System.out.println("resulttable Actualsize: " + resultTable.getSize());
	}
	
	/**
	 * Selection of a facet from the facetList.
	 * Update the filter on the facetValueList and refresh
	 * 
	 * @param fdn
	 * @param predicate
	 */
	public void selectFacet(FacetDirNode fdn, Node predicate) {
		this.fdn = fdn;
		this.selectedFacet = predicate;
		updateFacetValues();
	}
	
	public void changeFocus(FacetNode tmp) {
//		org.aksw.facete.v3.api.Direction dir =
//				Optional.of(direction)
//				.orElse(Optional.ofNullable(tmp.reachingDirection())
//						.orElse(fdn.dir()));

		
		org.aksw.facete.v3.api.Direction dir = tmp.reachingDirection();
		if(dir == null) {
			dir = fdn.dir();
		}
		
		tmp.chFocus();

		// For robustness ; dir should never be null
		if(dir != null) {
			fdn = tmp.step(dir);
		}
		
		updateFacets(fq);
		updateFacetPathPanel();
		facetList.takeFocus();

	}
	
	public void updateFacetPathPanel() {
		facetPathPanel.removeAllComponents();
		
		
		// Add a 'home button'
		facetPathPanel.addComponent(new Button("\u2302", () -> changeFocus(fdn.parent().root())));
		
		org.aksw.facete.v3.api.Direction dir = fdn.dir();
		
		// For each path element, create another button
		List<Directed<FacetNode>> path = fdn.parent().path();

		Set<Node> nodes = path.stream().map(Directed::getValue).map(FacetNode::reachingPredicate).collect(Collectors.toSet());
		Map<Node, String> labelMap = getLabels(nodes, Function.identity(), labelService, globalPrefixes);
		//Map<Node, String> labelMap = labelService.fetchMap(nodes);

		int n = path.size();
		for(int i = 0; i < n; ++i) {
			boolean isLastStep = i + 1 == n;

			Directed<FacetNode> step = path.get(i);

			FacetNode tmp = step.getValue();//current.step(current.reachingDirection());
			Node p = tmp.reachingPredicate();
			boolean isFwd = tmp.reachingDirection().isForward();
			String label = labelMap.get(p);
			Runnable action = () -> changeFocus(tmp);

			String str = (isFwd ? "" : "^") + label;

			if(!isLastStep) {
				facetPathPanel.addComponent(new Button(str, action));
			} else {
				facetPathPanel.addComponent(new Label2(str));				
			}
		}		

		switch(dir) {
		case FORWARD:
			facetPathPanel.addComponent(new Button(">", () -> setFacetDir(org.aksw.facete.v3.api.Direction.BACKWARD)));
			break;
		case BACKWARD:
			facetPathPanel.addComponent(new Button("<", () -> setFacetDir(org.aksw.facete.v3.api.Direction.FORWARD)));
			break;
		}

	

		
	}
	
	
	public DataQuery<FacetValueCount> facetValuesDataQuery() {
		UnaryRelation filter = Strings.isNullOrEmpty(facetValueFilter) ? null : KeywordSearchUtils.createConceptRegexIncludeSubject(BinaryRelationImpl.create(RDFS.label), facetValueFilter);
		
		DataQuery<FacetValueCount> base = fdn
				.facetValueCountsWithAbsent(includeAbsent)
				//.filter(filter)
				.filterUsing(filter, FacetValueCountImpl_.VALUE)
				.only(selectedFacet);
		
		
		int sortDirJena = sortDirMapJena[facetValueSortDir];
		switch(facetValueSortMode) {
		case 0:
			// base.addOrderBy(new NodePathletPath(Path.newPath()), sortDirJena);
			base.addOrderBy(new NodePathletPath(Path.newPath().fwd("http://www.example.org/value")), sortDirJena);
			break;
		case 1:
			base.addOrderBy(new NodePathletPath(Path.newPath().fwd("http://www.example.org/facetCount")), sortDirJena);
			break;
		}

		return base;
	}
	
	long facetValuePage[] = {0};
	public void updateFacetValues() {
		Stopwatch sw = Stopwatch.createStarted();
		
		if(fdn != null && selectedFacet != null) {
			// Set the title
			facetValuePanelBorder.setTitle("Facet Values" + " [" + selectedFacet + "]");
			
			facetValueList.setEnabled(false);

			DataQuery<FacetValueCount> base = facetValuesDataQuery();

			
			//Long count = fdn.facetValueCounts().count().blockingGet().getCount();
			Long count = base.count().blockingGet().getCount();
			// Update paginator with item count
			int itemsPerPage = 100;
			
			
			Paginator<Page> paginator = new PaginatorImpl(itemsPerPage);
			List<Page> pages = paginator.createPages(count, facetValuePage[0]);

			
			// Update the paginator buttons. If one has a focus, we memorize it so
			// we can restore it afterwards.
			boolean refocusPage = facetValuePagePanel.getChildren().stream()
					.anyMatch(child -> child instanceof Interactable && ((Interactable)child).isFocused());
			
			facetValuePagePanel.removeAllComponents();
			for(Page page : pages) {
//				if(page.isActive()) {
//					itemPagePanel.addComponent(new Label2("" + page.getPageNumber()));
//				} else {
					Button btn = new Button("" + page.getPageNumber(), () -> {
						facetValuePage[0] = page.getPageOffset();
						updateFacetValues();
					});
					facetValuePagePanel.addComponent(btn);
					
					if(page.isActive() && refocusPage) {
						btn.takeFocus();
					}
//				}
			}

			
			
			long itemStart = facetValuePage[0];
			long itemEnd = Math.min(itemStart + itemsPerPage, count);
			
			// TODO Update the title to include page info
//			String title = count == 0
//					? "(no matches)"
//					: String.format("Matches %d-%d of %d", itemStart + 1, itemEnd, count);
			
			// resultPanelBorder.setTitle(title);		
			
			
			List<FacetValueCount> fvcs = base		
					.offset(itemStart)
					.limit(itemsPerPage)
					.exec()
					.toList().blockingGet();
	
			//System.out.println("Got facet values:\n" + fvcs.stream().map(x -> x.getValue()).collect(Collectors.toList()));
			
			enrichWithLabels(fvcs, FacetValueCount::getValue, labelService, globalPrefixes);
			
			facetValueList.clearItems();
			for(FacetValueCount item : fvcs) {
				// check whether there is an equals constraint on that value
				HLFacetConstraint<? extends ConstraintFacade<? extends FacetNode>> tmp = fdn.via(item.getPredicate()).one().constraints().eq(item.getValue());
				boolean checked = tmp.isActive();
				
				facetValueList.addItem(item, checked);
				//facetValueList.addItem(fvc.getProperty(RDFS.label).getString() + " (" + fvc.getFocusCount().getCount() + ")");
				//facetValueList.set
	//			facetValueList.addItem(fvc.getProperty(RDFS.label).getString() + " (" + fvc.getFocusCount().getCount() + ")" , new Runnable() {
	//			@Override
	//			public void run() {
	//				System.out.println("run");
	//				// Code to run when action activated
	//			}
			//}
			//);
			}
			
			facetValueList.setEnabled(true);

		} else {
//			FacetValueCountImpl fcv = (FacetValueCountImpl)ModelFactory.createDefaultModel().createResource("http://fooar").as(FacetValueCount.class);
//			
//			fcv.addLiteral(Vocab.facetCount, 666l);
//			fcv.addLiteral(RDFS.label, "foobar");
//			facetValueList.addItem(fcv);
			facetValueList.clearItems();
			//facetValueList.addItem(null);
			// TODO Show in the panel that the list is empty
			facetValuePanelBorder.setTitle("Facet Values");
		}
		
		logger.info("updateFacetValues: " + sw.elapsed(TimeUnit.MILLISECONDS) / 1000.0 + "s");
	}
	
	public DataQuery<FacetCount> facetDataQuery(FacetedQuery fq) {
		UnaryRelation filter = Strings.isNullOrEmpty(facetFilter) ? null : KeywordSearchUtils.createConceptRegexIncludeSubject(BinaryRelationImpl.create(RDFS.label), facetFilter);
		
		DataQuery<FacetCount> base = fdn.facetCounts(includeAbsent)
				.filter(filter);

		int sortDirJena = sortDirMapJena[facetSortDir];
		switch(facetSortMode) {
		case 0:
			base.addOrderBy(new NodePathletPath(Path.newPath()), sortDirJena);
			break;
		case 1:
			base.addOrderBy(new NodePathletPath(Path.newPath().fwd("http://www.example.org/facetCount")), sortDirJena);
			break;
		}

		return base;
	}

	
	public void updateFacets(FacetedQuery fq) {
		Stopwatch sw = Stopwatch.createStarted();
		
		if(fdn != null) {
		
			int idx = facetList.getSelectedIndex();
			
			DataQuery<FacetCount> base = facetDataQuery(fq);
//			facetList.setEnabled(false);
			
					//.addOrderBy(new NodePathletPath(Path.newPath()), Query.ORDER_ASCENDING)
//					.addOrderBy(new NodePathletPath(Path.newPath().fwd("http://www.example.org/facetCount")), Query.ORDER_DESCENDING)
					
			List<FacetCount> fcs = base 
					.exec()
					.toList()
					// TODO This is still not idiomatic / we want to have a flow where we can cancel lable lookup
					.doOnSuccess(list -> enrichWithLabels(list, FacetCount::getPredicate, labelService, globalPrefixes))
					.blockingGet();
			//enrichWithLabels(fcs, FacetCount::getPredicate, labelService);
		
			logger.info("updateFacets [finished query]: " + sw.elapsed(TimeUnit.MILLISECONDS) / 1000.0 + "s");

			//List<Entry<FacetDirNode, >>fcs.stream().map(fc -> Maps.immutableEntry(fdn, fc)).collect(Collectors.toList());
			facetList.clearItems();
			
			
			for(FacetCount fc : fcs) {
					facetList.addItem(RunnableWithLabelAndData.from(
							toString(fc) + " (" + fc.getDistinctValueCount().getCount() + ")",
							fc.getPredicate(),
							() -> selectFacet(fdn, fc.getPredicate())));
			}
			
//			int newIdx = Math.min(facetList.getItemCount() - 1, idx);
//			facetList.setSelectedIndex(newIdx);
			
//			facetList.setEnabled(true);
		}

	
		logger.info("updateFacets: " + sw.elapsed(TimeUnit.MILLISECONDS) / 1000.0 + "s");
	}

	
	public static boolean isSparqlEndpoint(String url) {
		boolean result = false;
		try(RDFConnection conn = RDFConnectionFactory.connect(url)) {
			result = SparqlRx.execSelect(() -> conn.query("SELECT ?s { ?s a ?o } LIMIT 1"))
				.timeout(10, TimeUnit.SECONDS)
				.limit(1)
				.map(x -> true)
				.onErrorReturn(e -> false)
				.blockingSingle();
		}
		
		return result;
	}
	
	
	public static Query rewriteUnionDefaultGraph(Query q) {
		List<TernaryRelation> views = Arrays.asList(
				new TernaryRelationImpl(Concept.parseElement(
						"{ GRAPH ?g { ?s ?p ?o } }", null), Vars.s, Vars.p, Vars.o)
			);
		Query result = VirtualPartitionedQuery.rewrite(
				views,
				q);
//		System.out.println("Example 1\n" + example1);

		return result;
	}
	
	/**
	 * Entry point for the Facete3 Command Line Interface
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		
		// Turn on legacy mode; ISSUE #8 - https://github.com/hobbit-project/faceted-browsing-benchmark/issues/8
		JenaRuntime.isRDF11 = false;
		
		CmdFacete3Main cm = new CmdFacete3Main();
		
		// CommandCommit commit = new CommandCommit();
		JCommander jc = JCommander.newBuilder()
				.addObject(cm)
				.build();

		jc.parse(args);

        if (cm.help) {
            jc.usage();
            return;
        }
        
        
        
        
//		RDFConnection conn = RDFConnectionFactory.connect("http://dbpedia.org/sparql");
//		conn = wrapWithVirtualBnodeUris(conn, "jena", "<http://jena.apache.org/ARQ/function#bnode>");
//		try(QueryExecution qe = conn.query("SELECT * { ?s ?p ?o . FILTER(isBlank(?s)) } LIMIT 1")) {
//			System.out.println(ResultSetFormatter.asText(qe.execSelect()));
//		}
//
//		if(true) { return; }
		
		// TODO Move auto proxying to a proper test case
		//MapperProxyUtils.createProxyFactory(Test2.class);
//		JenaPluginUtils.scan(Test2.class.getPackage().getName());		
//		Test2 test = ModelFactory.createDefaultModel().createResource().as(Test2.class);
//		test.getFoo(Resource.class).add(RDFS.label);
//		test.getFoo(Integer.class).add(5);
//		test.getFoo(String.class).add("5");
//		
//		RDFDataMgr.write(System.err, test.getModel(), RDFFormat.TURTLE_PRETTY);
		
//		System.out.println(test.getFoo(Resource.class));
//		System.out.println(test.getFoo(String.class));
//		System.out.println(test.getFoo(Integer.class));
		
//		for(Method m : Test2.class.getDeclaredMethods()) {
//			MapperProxyUtils.canActAsCollectionView(m, Set.class, Resource.class);
//			MapperProxyUtils.canActAsCollectionView(m, Set.class, Object.class);
//		}
		
//		if(true) return;
			    

	    List<String> files = cm.nonOptionArgs;
	    
	    
	    RDFConnection conn = null;
	    try {
	    	conn = createConnectionFromArgs(cm.nonOptionArgs, cm.bnodeProfile);
			
    	    if(!cm.defaultGraphs.isEmpty()) {
    	    	if(cm.defaultGraphs.size() > 1) {
    	    		throw new RuntimeException("Only 1 default graph supported with this version");
    	    	}
    	    	
    			Node g = NodeFactory.createURI(cm.defaultGraphs.get(0));

    			conn = RDFConnectionFactoryEx.wrapWithQueryTransform(conn,
    	    			q -> {
    	    				Transform t = new TransformGraphRename(Quad.defaultGraphNodeGenerated, g) ;
    	    				
    	    				Query r = QueryUtils.applyOpTransform(q, op -> {
    	    					op = Algebra.toQuadForm(op);
    		    		        op = Transformer.transform(t, op);
    		    		        //op = AlgebraUtils.createDefaultRewriter().rewrite(op);
    		    		        return op;
    	    				});
    	    				
    	    				return r;
    	    			});
    	    }

    	    if(cm.unionDefaultGraphMode) {
    	    	conn = RDFConnectionFactoryEx.wrapWithQueryTransform(conn,
    	    			q -> QueryUtils.applyOpTransform(q, Algebra::unionDefaultGraph));
    	    }

    	    Iterable<String> prefixSources = Iterables.concat(
    	    		Collections.singleton("rdf-prefixes/prefix.cc.2019-12-17.jsonld"),
    	    		cm.prefixSources);

    		PrefixMapping prefixes = new PrefixMappingImpl();
            for(String source : prefixSources) {
            	PrefixMapping tmp = RDFDataMgr.loadModel(source);
            	prefixes.setNsPrefixes(tmp);
            }

		    UnaryRelation baseConcept = Strings.isNullOrEmpty(cm.baseConcept)
		    		? null
		    		: parseConcept(cm.baseConcept, prefixes);
		    
			FacetedQuery fq = FacetedQueryImpl.create(conn);

			if(baseConcept != null) {
				fq.baseConcept(baseConcept);
			}


			
			new MainCliFacete3().init(conn, prefixes, fq);
	    } catch(Exception e) {
	    	// The exception may not be visible if logging is disabled - so print it out here
	    	e.printStackTrace();
	    	throw new RuntimeException(e);
	    } finally {
	    	if(conn != null) {
	    		conn.close();
	    	}
	    }
	}
	
	
	public static RDFConnection createConnectionFromArgs(
			Collection<String> files,
			String bnodeProfile) {
		
		RDFConnection conn = null;
	    if(files.size() == 1) {
	    	logger.info("Probing argument for SPARQL endpoint");
	    	String str = files.iterator().next(); //.get(0);
	    	boolean isSparql = isSparqlEndpoint(str);

	    	if(isSparql) {
	    		logger.info("Probe query succeeded. Connecting with bnode profile " + bnodeProfile + " ...");

	    		conn = RDFConnectionRemote.create()
	    				//.acceptHeaderGraph(WebContent.contentTypeRDFXML)
	    				.acceptHeaderSelectQuery(WebContent.contentTypeResultsXML)
	    				.destination(str)
	    				.build();

	    		if("auto".equalsIgnoreCase(bnodeProfile)) {
	    			
	    			Map<String, String> env = Collections.singletonMap("REMOTE", str);
	    			Model report = ModelFactory.createDefaultModel();
	    			RDFDataMgrEx.execSparql(report, "probe-endpoint-dbms.sparql", env::get);
	    			Property dbmsShortName = ResourceFactory.createProperty("http://www.example.org/dbmsShortName");

	    			List<String> nodes = report.listObjectsOfProperty(dbmsShortName)
	    				.mapWith(n -> n.isLiteral() ? Objects.toString(n.asLiteral().getValue()) : null)
	    				.toList();
	    			String first = Iterables.getFirst(nodes, null);
	    			
	    			if(first != null) {
	    				bnodeProfile = first;
	    			}
	    		}
	    		
	    		if(bnodeProfile != null) {
	    			conn = wrapWithVirtualBnodeUris(conn, bnodeProfile);
	    		} else {
	    			logger.warn("No bnode profile found - bnodes are not supported");
	    		}
	    	}
	    }
	    
	    if(conn == null) {
		    //Model model = ModelFactory.createDefaultModel();
	    	Dataset dataset = DatasetFactory.create();
		    Stopwatch sw = Stopwatch.createStarted();
		    logger.info("Loading RDF files...");
		    for(String file : files) {
			    logger.info("  Attempting to loading " + file);
		    	//Model tmp = RDFDataMgr.loadModel(file);
		    	//model.add(tmp);
			    RDFDataMgr.read(dataset, file);
		    }
//		    logger.info("Done loading " + ds.size() + " triples in " + sw.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0 + " seconds.");
		    logger.info("Done loading dataset in " + sw.stop().elapsed(TimeUnit.MILLISECONDS) / 1000.0 + " seconds.");
		    
		    //Dataset dataset = DatasetFactory.wrap(model);
		    conn = RDFConnectionFactory.connect(dataset);
			conn = wrapWithVirtualBnodeUris(conn, "jena");
	    }



		return conn;
	}

	
	public void setFacetDir(org.aksw.facete.v3.api.Direction dir) {
		fdn = fdn.parent().step(dir);
		updateFacetPathPanel();
		updateFacets(fq);
		
		facetList.takeFocus();
	}
	
	
	public RDFNode fetchIfResource(Node node) {
		Query q = QueryFactory.create("CONSTRUCT WHERE { ?s ?p ?o }");
		UnaryRelation filter = ConceptUtils.createFilterConcept(node);
		q.setQueryPattern(RelationImpl.create(q.getQueryPattern(), Vars.s).joinOn(Vars.s)
				.with(filter).getElement());
		
		
		Model model = fq.connection().queryConstruct(q);
		RDFNode result = model.asRDFNode(node);
		
		return result;
		//QueryGenerationUtils.
		//QueryGenerationUtils.
	}

	
	public static RDFConnection wrapWithVirtualBnodeUris(RDFConnection conn, String profile) {
		//ExprTransformVirtualBnodeUris xform = new ExprTransformVirtualBnodeUris(vendorLabel, bnodeLabelFn);
		
		Model model = RDFDataMgr.loadModel("bnode-rewrites.ttl");
		RDFDataMgrEx.execSparql(model, "udf-inferences.sparql");

		Set<String> profiles = new HashSet<>(Arrays.asList("http://ns.aksw.org/profile/" + profile));
		Map<String, UserDefinedFunctionDefinition> macros = UserDefinedFunctions.load(model, profiles);
		
		ExprTransformVirtualBnodeUris xform = new ExprTransformVirtualBnodeUris(macros);

		
		RDFConnection result = RDFConnectionFactoryEx.wrapWithQueryTransform(conn, xform::rewrite);
		return result;
	}
	
	public void init(RDFConnection conn, PrefixMapping pm, FacetedQuery fq) throws Exception
	{
		this.globalPrefixes.setNsPrefixes(pm);
		this.fq = fq;

		resourceTable.setCellSelection(true);
		resultPanelBorder = (StandardBorder)Borders.singleLine("Matches");

		facetValuePanelBorder = (StandardBorder)Borders.singleLine("Facet Values");

		resourcePanelBorder = (StandardBorder)Borders.singleLine("Resource");

		Stopwatch sw = Stopwatch.createStarted();
		
		
//		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();

		
		//Dataset dataset = RDFDataMgr.loadDataset("/home/raven/.dcat/repository/datasets/data/dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04/_content/dcat.ttl");
//		Dataset dataset = RDFDataMgr.loadDataset("path-data-simple.ttl");
		//conn = RDFConnectionFactory.connect("http://localhost:5000/provenance");
		
		

		//facetList.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		facetList.setInputFilter((i, keyStroke) -> {
			
			@SuppressWarnings("unchecked")
			RunnableWithLabelAndData<Node> pr = (RunnableWithLabelAndData<Node>)facetList.getSelectedItem();
			Node node = pr == null ? null : pr.getData();
			
//			setResourcePanelTitle(node);
			
			// Navigation; set the facetDirNode
			Character c = keyStroke.getCharacter();
			if(c != null) {
				switch(c) {
				case resourceViewKey:
					if(node != null) {
						RDFNode rdfNode = fetchIfResource(node);
						updateResourceView(rdfNode);
					}
					break;
				case showQueryKey:
					Entry<Node, Query> pq = facetDataQuery(fq).toConstructQuery();
					// TODO Make prefix support part of the DataQuery API
					QueryUtils.optimizePrefixes(pq.getValue(), globalPrefixes);
					
//					new TextInputDialogBuilder()
//						.setTitle("Facet Query")
//						.setInitialContent(pq.toString())
//						.build()
//						.showDialog((WindowBasedTextGUI)i.getTextGUI());
					new MessageDialogBuilder()
						.setTitle("Facets rooted in " + pq.getKey())
						.setText("" + pq.getValue())
						.build()
						.showDialog((WindowBasedTextGUI)i.getTextGUI());
					break;
				}
			}
			
			if(KeyType.Backspace.equals(keyStroke.getKeyType())) {
//				PseudoRunnable<Node> pr = (PseudoRunnable<Node>)facetList.getSelectedItem();
//				Node node = pr.getData();

//				ses.schedule(() -> {
					org.aksw.facete.v3.api.Direction dir = fdn.dir();
					fq.focus().step(node, dir).one().chFocus();
					fdn = fq.focus().step(dir);
					
					updateFacets(fq);
					updateFacetPathPanel();
					return false;
//				}, 1, TimeUnit.SECONDS);
			}	
				
			if(KeyType.ArrowRight.equals(keyStroke.getKeyType())) {
				facetValueList.takeFocus();
			}

			return true;
		});
		
		
		
		fdn = fq.focus().fwd();

		
		
//		facetList.setInputFilter((i, keyStroke) -> {
//			facetList.g
//			
//			if(keyStroke.getKeyType().equals(KeyType.Enter)) {
//				facetFilter = facetFilterBox.getText();
//				updateFacets(fq);
//				return false;
//			}
//			
//			return true;			
//		});
		
		labelService = LookupServiceUtils
				.createLookupService(fq.connection(), BinaryRelationImpl.create(RDFS.label))
				.partition(10)
				.cache()
				.mapValues((k, vs) -> vs.isEmpty() ? deriveLabelFromIri(k.getURI()) : vs.iterator().next())
				.mapValues((k, v) -> "" + v);
		
		// Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory()
        		.setTerminalEmulatorTitle("Facete III")
        		.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE)
        		.createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        
        //WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

//		TerminalSize size = new TerminalSize(80, 25);
		
		Panel mainPanel = new Panel();
		
		
		TextBox facetFilterBox = new TextBox(); // new TerminalSize(16, 1))
//		ComboBox<String> facetFilterBox = new ComboBox<>();
//		facetFilterBox.setPreferredSize(new TerminalSize(16, 1));
//		facetFilterBox.addItem("foo");
//		facetFilterBox.addItem("bar");
//		facetFilterBox.addItem("baz");
		
		
		facetFilterBox.setInputFilter((i, keyStroke) -> {
			if(keyStroke.getKeyType().equals(KeyType.Enter)) {
				facetFilter = facetFilterBox.getText();
				updateFacets(fq);
				return false;
			}
			
			return true;
		});
	
		
		updateFacets(fq);
		updateItems(fq);
		
		// Prevent focus change on down arrow key when at end of list 
		facetValueList.setInputFilter((i, keyStroke) -> {
			boolean r = true;

			Character c = keyStroke.getCharacter();
			
			FacetValueCount item = facetValueList.getSelectedItem();
//			setResourcePanelTitle(item.asNode());

			if(c != null) {				
				switch(c) {
				case resourceViewKey:
					if(item != null) {
						Node node = item.getValue();
						RDFNode rdfNode = fetchIfResource(node);
						updateResourceView(rdfNode);
					}
					r = false;
					break;
				case showQueryKey:
					Entry<Node, Query> pq = facetValuesDataQuery().toConstructQuery();
					// TODO Make prefix support part of the DataQuery API
					QueryUtils.optimizePrefixes(pq.getValue(), globalPrefixes);

					new MessageDialogBuilder()
						.setTitle("Facet values rooted in " + pq.getKey())
						.setText("" + pq.getValue())
						.build()
						.showDialog((WindowBasedTextGUI)i.getTextGUI());

//					MessageDialog.showMessageDialog(
//					(WindowBasedTextGUI)i.getTextGUI(),
//					"Facet values with root var " + pq.getKey(),
//					"" + pq.getValue());
					r = true;
					break;
				}
			}

			
			if(KeyType.ArrowLeft.equals(keyStroke.getKeyType())) {
				facetList.takeFocus();
			}

//			r = !(keyStroke.getKeyType().equals(KeyType.ArrowUp) && facetValueList.getSelectedIndex() == 0) &&
			r = r && !(keyStroke.getKeyType().equals(KeyType.ArrowDown) && facetValueList.getItems().size() - 1 == facetValueList.getSelectedIndex());
			return r;
		});

		
		facetValueList.addListener((int itemIndex, boolean checked) -> {
			FacetValueCount item = facetValueList.getItemAt(itemIndex);
			//System.out.println(item);

			Node v = item.getValue();
			HLFacetConstraint<? extends ConstraintFacade<? extends FacetNode>> tmp = fdn.via(item.getPredicate()).one().constraints().eq(v);
			tmp.setActive(checked);
			
			
			updateFacets(fq);
			updateItems(fq);
			updateConstraints(fq);

		});

		facetValueList.setListItemRenderer(new CheckBoxList.CheckBoxListItemRenderer<FacetValueCount>() {
			@Override
			public String getLabel(CheckBoxList<FacetValueCount> listBox, int index, FacetValueCount item) {
	            boolean checked = listBox.isChecked(index);
	            String check = checked ? "x" : " ";
	            
	            String text = Optional.ofNullable(item.getProperty(RDFS.label)).map(Statement::getString).orElse("(null)") + " (" + item.getFocusCount().getCount() + ")"; //item.toString();
	            return "[" + check + "] " + text;
			};
		});

		
		resultTable.setCellSelection(true);
		
		resultTable.setInputFilter((i, keyStroke) -> {
			Character c = keyStroke.getCharacter();
			int x = resultTable.getSelectedRow();
			int y = resultTable.getSelectedColumn();
			
			RDFNode node = getCell(resultTable.getTableModel(), x, y);
			//Node node = x >= 0 && y >= 0 ? resultTable.getTableModel().getCell(x, y) : null;
			
//			setResourcePanelTitle(node.asNode());
			
			if(c != null) {				
				switch(c) {
				case resourceViewKey:
					if(node != null) { 
						RDFNode rdfNode = fetchIfResource(node.asNode());
						updateResourceView(rdfNode);
					}
					break;
				case showQueryKey:
					Entry<Node, Query> pq = itemsDataQuery(fq).toConstructQuery();
					// TODO Make prefix support part of the DataQuery API
					QueryUtils.optimizePrefixes(pq.getValue(), globalPrefixes);

					new MessageDialogBuilder()
						.setTitle("Matching items rooted in " + pq.getKey())
						.setText("" + pq.getValue())
						.build()
						.showDialog((WindowBasedTextGUI)i.getTextGUI());
					break;
				}
			}
			
			return true;
		});
//		FacetDirNode fdn2 = fq.focus().fwd();

		
		
		updateFacetValues();
		
		resourceTable.setInputFilter((i, keyStroke) -> {
			Character c = keyStroke.getCharacter();
			int x = resourceTable.getSelectedRow();
			int y = resourceTable.getSelectedColumn();
			
//			Node node = x >= 0 && y >= 0 ? resourceTable.getTableModel().getCell(x, y) : null;
			RDFNode node = getCell(resourceTable.getTableModel(), x, y);
//			setResourcePanelTitle(node.asNode());

			
			if(c != null) {				
				switch(c) {
				case resourceViewKey:
					if(node != null) { 
						RDFNode rdfNode = fetchIfResource(node.asNode());
						updateResourceView(rdfNode);
					}
				}
			}
			
			return true;
		});

		

		Panel facetFilterPanel = new Panel();
		
//		Button btnClear = new Button("Clr") {
//			public synchronized com.googlecode.lanterna.gui2.Interactable.Result handleKeyStroke(KeyStroke keyStroke) {
//				if(keyStroke.getKeyType().equals(KeyType.Enter)) {
//					facetFilterBox.setText("");
//				}
//				return super.handleKeyStroke(keyStroke);
//			};
//		};
//		facetFilterPanel.addComponent(btnClear);

		Button facetFilterClearBtn = new Button("Clr");
		facetFilterClearBtn.addListener(btn -> {
			facetFilter = null;
			facetFilterBox.setText("");
			updateFacets(fq);
		});
		
		
		Panel facetSortPanel = new Panel();
		Button facetSortModeToggle = new Button(sortModeLabel[facetSortMode]);
		Button facetSortDirToggle = new Button(sortDirLabel[facetSortDir]);

		
		facetSortModeToggle.addListener(btn -> {
			facetSortMode = (facetSortMode + 1) % 2;
			facetSortModeToggle.setLabel(sortModeLabel[facetSortMode]);
			updateFacets(fq);
		});

		facetSortDirToggle.addListener(btn -> {
			facetSortDir = (facetSortDir + 1) % 2;
			facetSortDirToggle.setLabel(sortDirLabel[facetSortDir]);
			updateFacets(fq);
		});

		
//		Button btnApply = new Button("Clr") {
//			public synchronized com.googlecode.lanterna.gui2.Interactable.Result handleKeyStroke(KeyStroke keyStroke) {
//				if(keyStroke.getKeyType().equals(KeyType.Enter)) {
//					MessageDialog.showMessageDialog(textGUI, "test", "test");
//				}
//				return super.handleKeyStroke(keyStroke);
//			};
//		};
		
		
		
				//GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING, true, true, 2, 1)));
		
		
		TextBox facetValueFilterBox = new TextBox(); // new TerminalSize(16, 1))

		
		facetValueFilterBox.setInputFilter((i, keyStroke) -> {
			if(keyStroke.getKeyType().equals(KeyType.Enter)) {
				facetValueFilter = facetValueFilterBox.getText();
				updateFacetValues();
				return false;
			}
			
			return true;
		});

		Button facetValueFilterClearBtn = new Button("Clr");
		facetValueFilterClearBtn.addListener(btn -> {
			facetValueFilter = null;
			facetValueFilterBox.setText("");
			updateFacetValues();
		});

		Panel facetValueFilterPanel = new Panel();

		
		
		
		Panel facetValuePanel = new Panel();
		Panel facetValueSortPanel = new Panel();
		Button facetValueSortModeToggle = new Button(sortModeLabel[facetValueSortMode]);
		Button facetValueSortDirToggle = new Button(sortDirLabel[facetValueSortDir]);

		
		facetValueSortModeToggle.addListener(btn -> {
			facetValueSortMode = (facetValueSortMode + 1) % 2;
			facetValueSortModeToggle.setLabel(sortModeLabel[facetValueSortMode]);
			updateFacetValues();
		});

		facetValueSortDirToggle.addListener(btn -> {
			facetValueSortDir = (facetValueSortDir + 1) % 2;
			facetValueSortDirToggle.setLabel(sortDirLabel[facetValueSortDir]);
			updateFacetValues();
		});

		constraintList.addListener((int itemIndex, boolean checked) -> {
			HLFacetConstraint<?> item = constraintList.getItemAt(itemIndex);
			//System.out.println(item);

			item.deactivate();
			
			updateFacets(fq);
			updateFacetValues();
			updateItems(fq);
			updateConstraints(fq);
		});
		constraintList.setListItemRenderer(new CheckBoxList.CheckBoxListItemRenderer<HLFacetConstraint<?>>() {
			@Override
			public String getLabel(CheckBoxList<HLFacetConstraint<?>> listBox, int index, HLFacetConstraint<?> item) {
	            boolean checked = listBox.isChecked(index);
	            String check = checked ? "x" : " ";

	            String text = MainCliFacete3.toString(item, labelService, globalPrefixes);
	            return "[" + check + "] " + text;
			};
		});



		Panel facetPanel = new Panel();
		Panel constraintPanel = new Panel();
		Panel resultPanel = new Panel();

		Panel cartPanel = new Panel();

		// Component hierarchy and layouts
		

		facetSortPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.BEGINNING, Alignment.CENTER, true, false, 1, 1)); //GridLayout.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
		facetSortPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
		facetSortPanel.addComponent(facetSortModeToggle);
		facetSortPanel.addComponent(facetSortDirToggle);

		facetPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, false, false, 1, 1)); //.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));
		facetPanel.setLayoutManager(new GridLayout2(1));
		facetPanel.addComponent(facetFilterPanel.withBorder(Borders.singleLine("Filter")));
		facetPanel.addComponent(facetPathPanel);
		facetPanel.addComponent(facetSortPanel);
		facetPanel.addComponent(facetList);

		facetFilterBox.setLayoutData(GridLayout2.createHorizontallyFilledLayoutData(1));

		facetFilterPanel.setLayoutData(GridLayout2.createHorizontallyFilledLayoutData(1));
		facetFilterPanel.setLayoutManager(new GridLayout2(2));
		facetFilterPanel.addComponent(facetFilterBox);
		facetFilterPanel.addComponent(facetFilterClearBtn);
		
		facetPathPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.BEGINNING, Alignment.CENTER, true, false, 1, 1)); //GridLayout.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
		facetPathPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
//		facetPathPanel.addComponent(new Button("Foo"));
		
		updateFacetPathPanel();
		
		facetValueFilterBox.setLayoutData(GridLayout2.createHorizontallyFilledLayoutData(1));
		
		facetValueFilterPanel.setLayoutData(GridLayout2.createHorizontallyFilledLayoutData(1));
		facetValueFilterPanel.setLayoutManager(new GridLayout2(2));
		facetValueFilterPanel.addComponent(facetValueFilterBox);
		facetValueFilterPanel.addComponent(facetValueFilterClearBtn);

		facetValueSortPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.BEGINNING, Alignment.CENTER, true, false, 1, 1)); //GridLayout.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
		facetValueSortPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
		facetValueSortPanel.addComponent(facetValueSortModeToggle);
		facetValueSortPanel.addComponent(facetValueSortDirToggle);

		facetValuePagePanel.setLayoutData(GridLayout2.createLayoutData(Alignment.BEGINNING, Alignment.CENTER, true, false, 1, 1)); //GridLayout.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
		facetValuePagePanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

		facetValuePanel.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
		facetValuePanel.setLayoutManager(new GridLayout2(1));
		facetValuePanel.addComponent(facetValueFilterPanel.withBorder(Borders.singleLine("Filter")));		
		facetValuePanel.addComponent(facetValueSortPanel);
		facetValuePanel.addComponent(facetValuePagePanel);
		facetValuePanel.addComponent(facetValueList);


		facetList.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
		facetValueList.setLayoutData(GridLayout2.createHorizontallyFilledLayoutData(1));
		constraintList.setLayoutData(GridLayout2.createHorizontallyFilledLayoutData(1));

		constraintPanel.setLayoutData(GridLayout2.createLayoutData(GridLayout2.Alignment.FILL, GridLayout2.Alignment.BEGINNING, true, false, 2, 1));
		constraintPanel.addComponent(constraintList);


		
		itemPagePanel.setLayoutData(GridLayout2.createLayoutData(Alignment.BEGINNING, Alignment.CENTER, true, false, 1, 1)); //GridLayout.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
		itemPagePanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

		resultTable.setTableCellRenderer(new DefaultTableCellRenderer2Rdf());
		resultTable.setRenderer(new DefaultTableRenderer2<RDFNode>());
		resultTable.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, true, 1, 1));

		resultPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, true, 1, 1));
		resultPanel.setLayoutManager(new GridLayout2(1));
		resultPanel.addComponent(itemPagePanel);
		resultPanel.addComponent(resultTable);
		
		resourceTable.setTableCellRenderer(new DefaultTableCellRenderer2Rdf() {
			public String toString(RDFNode node, int columnIndex, int rowIndex) {
				return columnIndex != 1 ? super.toString(node, columnIndex, rowIndex) : Objects.toString(node);
			};
		});
		resourceTable.setRenderer(new DefaultTableRenderer2<RDFNode>());
		resourceTable.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, true, 1, 1));

		resourcePanel.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, true, 1, 1));
		resourcePanel.setLayoutManager(new GridLayout2(1));
		
		resourceSubjectLabel.setLineWrapper(MainCliFacete3::wrapLines);
		resourcePanel.addComponent(resourceSubjectLabel);
		resourcePanel.addComponent(resourceTable);

// TODO re-enable cart panel once its working
//		cartPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, true, 1, 1));
//		cartPanel.setLayoutManager(new GridLayout2(1));
//		//resultPanel.addComponent(itemPagePanel);
//		cartPanel.addComponent(cartTable);

		


		//mainPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.FILL, true, true, 2, 1));
		mainPanel.setLayoutManager(new GridLayout2(2));
		mainPanel.addComponent(facetPanel.withBorder(Borders.singleLine("Facets")));
		mainPanel.addComponent(facetValuePanel.withBorder(facetValuePanelBorder));
		mainPanel.addComponent(constraintPanel.withBorder(Borders.singleLine("Constraints")));
		
		mainPanel.addComponent(resultPanel.withBorder(resultPanelBorder));
		mainPanel.addComponent(resourcePanel.withBorder(resourcePanelBorder));
//		mainPanel.addComponent(cartPanel.withBorder(Borders.singleLine("Cart")));

		
		 // Create window to hold the panel
        BasicWindow window = new BasicWindow();
        window.setComponent(mainPanel);
        

        window.addWindowListener(new WindowListener() {
			@Override
			public void onUnhandledInput(Window basePane, KeyStroke keyStroke, AtomicBoolean hasBeenHandled) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
				Character c = keyStroke.getCharacter() == null ? null : Character.toLowerCase(keyStroke.getCharacter());
				
				if(KeyType.Escape.equals(keyStroke.getKeyType()) ||
						Character.valueOf('q').equals(c)) {
					MessageDialogButton selected = new MessageDialogBuilder()
						.setTitle("")
						.setText("Close this application?")
						.addButton(MessageDialogButton.No)
						.addButton(MessageDialogButton.Yes)
						.build()
						.showDialog((WindowBasedTextGUI)window.getTextGUI());
					
					switch(selected) {
					case Yes:
						window.close();
						try {
							terminal.close();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						break;
					default:
						break;
					}
				}

				//keyStroke.getCharacter()
				
				// TODO Auto-generated method stub
				//System.out.println("Test " + basePane);
			}
			
			@Override
			public void onResized(Window window, TerminalSize oldSize, TerminalSize newSize) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onMoved(Window window, TerminalPosition oldPosition, TerminalPosition newPosition) {
				// TODO Auto-generated method stub
				
			}
		});
        
        
        
        
        //Window.Hint.NO_POST_RENDERING
        // Create gui and start gui

		MultiWindowTextGUI gui = new MultiWindowTextGUI(
        		screen,
        		new DefaultWindowManager(),
        		new EmptySpace(TextColor.ANSI.BLACK));
		

//
//        MultiWindowTextGUI gui = new MultiWindowTextGUI(
//        		screen,
//        		new DefaultWindowManager(),
//                null, //new WindowShadowRenderer(),
//        		new EmptySpace(TextColor.ANSI.BLUE));
		
        // window.setHints(Collections.singleton(Window.Hint.NO_POST_RENDERING));

        window.setHints(Arrays.asList(Window.Hint.NO_POST_RENDERING, Window.Hint.EXPANDED, Window.Hint.FIT_TERMINAL_WINDOW));
        
		selectFacet(fdn, RDF.type.asNode());

        gui.addWindowAndWait(window);

	}
	
	
	public static String toString(RDFNode node) {
		Resource r = node.isResource() ? node.asResource() : null;
		
		String result = r != null
			? Optional.ofNullable(r.getProperty(RDFS.label))
					.map(Statement::getString)
					.orElse(r.isURIResource()
							? MainCliFacete3.deriveLabelFromIri(r.getURI())
							: r.getId().getLabelString())
			: Objects.toString(Optional.ofNullable(node)
					.map(RDFNode::asNode)
					.orElse(null));
				
//				System.out.println("RESULT: " + result + " for " + node);
		return result;
	}

	public void setResourcePanelTitle(Node node) {
		String str = "Resource" + (node == null ? "" : " [" + node + "]");
		
//		System.out.println("Setting Title: " + node);
		resourcePanelBorder.setTitle(str);
	}
	
//	public static void enrichWithLabels(Collection<FacetValueCount> cs, LookupService<Node, String> labelService) {
//		Map<Node, FacetCount> index = Maps.uniqueIndex(cs, FacetCount::getPredicate);		
//		Map<Node, String> map = labelService.fetchMap(index.keySet());
//		index.forEach((k, v) -> v.addLiteral(RDFS.label, map.getOrDefault(k, k.getLocalName())));
//	}
	
	public static List<String> wrapLines(int cols, String[] lines) {
		List<String> result = Arrays.asList(lines).stream()
				.map(line -> cropString(line, cols, 0, tooMany -> "\u2026"))
				.collect(Collectors.toList());
		return result;
	}
	
    public static String cropString(String str, int nMax, int nTolerance, Function<Integer, String> suffixFn)
    {
        String result = str;
        int nGiven = str.length();
        
        if(nGiven > nMax) {
            int tooMany = nGiven - nMax;
            
            if(tooMany > nTolerance) {
            	String suffix = suffixFn.apply(tooMany);
            	int suffixLength = suffix.length();
            	int availSuffix = Math.min(nMax, suffixLength);

            	int subLength = Math.max(0, nMax - suffixLength);
            	result = str.substring(0, subLength) + suffix.substring(0, availSuffix);
//                result = str.substring(0, nMax) +
//                    "... (" + tooMany + " more bytes)";
            }
        }
        return result;
    }


	public static <T extends RDFNode> void enrichWithLabels(
			Collection<T> cs, Function<? super T, ? extends Node> nodeFunction,
			LookupService<Node, String> labelService,
			PrefixMapping prefixes) {
		// Replace null nodes with Node.NULL
		// TODO Use own own constant should jena remove this deprecated symbol
		logger.info("enrichWithLabels: Lookup of size " + cs.size());
		
		Multimap<Node, T> index = Multimaps.index(cs, item ->
			Optional.<Node>ofNullable(nodeFunction.apply(item)).orElse(NodeUtils.nullUriNode));

		//Map<Node, T> index = Maps.uniqueIndex();
		Set<Node> s = index.keySet().stream()
				.filter(Node::isURI)
				.collect(Collectors.toSet());

		Map<Node, String> map = labelService.fetchMap(s);
		index.forEach((k, v) -> v.asResource().addLiteral(RDFS.label,
				map.getOrDefault(k, NodeUtils.nullUriNode.equals(k)
						? "(null)"
						: k.isURI()
							? deriveLabelFromIri(k.getURI())
							: formatLiteralNode(k, prefixes))));
							//: NodeFmtLib.str(k, "", riotPrefixMap))));
							//: k.toString())));
	}

	public static String deriveLabelFromIri(String iriStr) {

		String result;
		for(;;) {
			// Split XML returns invalid out-of-bound index for <http://dbpedia.org/resource/Ada_Apa_dengan_Cinta%3>
			// This is what Node.getLocalName does
			int idx = Util.splitNamespaceXML(iriStr);
			result = idx == -1 || idx > iriStr.length() ? iriStr : iriStr.substring(idx);
			if(result.isEmpty() && !iriStr.isEmpty() && idx != -1) {
				iriStr = iriStr.substring(0, iriStr.length() - 1);
				continue;
			} else {
				break;
			}
		};	
		return result;
	}

	
	public static <T> Collection<Labeled<T>> doLabel(Collection<T> cs, Function<? super T, ? extends String> itemToLabel) {
		Collection<Labeled<T>> result = cs.stream()
				.map(item -> new LabeledImpl<T>(item, itemToLabel.apply(item)))
				.collect(Collectors.toList());

		return result;
	}

	public static String formatLiteralNode(Node node, PrefixMapping prefixMapping) {
		String result;
		if(node.isLiteral()) {
			String dtIri = node.getLiteralDatatypeURI();
			String dtPart = null;
			if(dtIri != null) {
				Entry<String, String> prefixToIri = PrefixUtils.findLongestPrefix(prefixMapping, dtIri);
				dtPart = prefixToIri != null 
					? prefixToIri.getKey() + ":" + dtIri.substring(prefixToIri.getValue().length())
					: "<" + dtIri + ">";
			}
			
			result = "\"" + node.getLiteralLexicalForm() + "\""
					+ (dtPart == null ? "" : "^^" + dtPart); 			
		} else {
			result = Objects.toString(node);
		}
		
		return result;
	}
	
	public static <T> Map<T, String> getLabels(
			Collection<T> cs, Function<? super T, ? extends Node> nodeFunction,
			LookupService<Node, String> labelService,
			PrefixMapping prefixes) {
		Multimap<Node, T> index = Multimaps.index(cs, nodeFunction::apply);
		//Map<Node, T> index = Maps.uniqueIndex();
		Set<Node> s = index.keySet().stream().filter(Node::isURI).collect(Collectors.toSet());
		Map<Node, String> map = labelService.fetchMap(s);
		
		// TODO Avoid copying the prefix map all the time
		Function<Node, String> determineLabel = k -> map.getOrDefault(k, k.isURI()
				? deriveLabelFromIri(k.getURI())
				: formatLiteralNode(k, prefixes));
				// : NodeFmtLib.str(k, "", riotPrefixMap));
				//: k.toString()); 
		
		Map<T, String> result =
			index.entries().stream().map(
			e -> Maps.immutableEntry(e.getValue(), determineLabel.apply(e.getKey())))
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		return result;
		//index.forEach((k, v) -> v.addLiteral(RDFS.label, map.getOrDefault(k, k.isURI() ? k.getLocalName() : k.toString())));
	}

}
