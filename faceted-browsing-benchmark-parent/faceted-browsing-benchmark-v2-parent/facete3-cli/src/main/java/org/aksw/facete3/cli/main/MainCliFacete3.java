package org.aksw.facete3.cli.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.aksw.commons.accessors.SingleValuedAccessor;
import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.impl.ConstraintFacadeImpl;
import org.aksw.facete.v3.impl.FacetNodeImpl;
import org.aksw.facete.v3.impl.FacetNodeResource;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.facete.v3.impl.FacetedQueryResource;
import org.aksw.facete.v3.impl.HLFacetConstraintImpl;
import org.aksw.facete3.cli.main.GridLayout2.Alignment;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.vocabulary.RDFS;
import org.hobbit.benchmark.faceted_browsing.v2.main.KeywordSearchUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.CheckBoxList;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;

import jersey.repackaged.com.google.common.collect.Maps;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;


// If we wanted to create an Amazon like faceted interface we'd need:
// Ranking function over a set of values - rank(concept) -> BinaryRelation (resource, rank-value)
// Rank is actually just a 'combine'* with a ranking attribute
// We can then sort the binary relation by rank
// binaryRelation.clearOrder(); binaryRelation.addOrder(binaryRelation.getTargetVar(), 1)
// * my current term for the generalization of a sparql join on the syntax level - which isn't necessarily a join in the first place


// Idea for an api to allow for something similar to angularjs' dirty checking
class DirtyChecker {
	
	
	public static <S1> Runnable watch(Supplier<S1> s1, Consumer<? super S1> action) {
		return null;
	}
	
	
	// Bind a computation to a getter/setter
	public static <T, S1> Runnable bind(
			SingleValuedAccessor<T> target,
			Supplier<S1> s1, 
			Function<? super S1, ? extends T> fn) {
		return null;
	}
	
	public static <T, S1, S2> Runnable bind(
			SingleValuedAccessor<T> target,
			Supplier<S1> s1, Supplier<S2> s2,
			BiFunction<? super S1, ? super S2, ? extends T> fn) {
		return null;
	}
	
}

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
	
	public static String treeToString() {
		return null;
	}
	


	public static BgpNode HACK = ModelFactory.createDefaultModel().createResource("should not appear anywhere").as(BgpNode.class);

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
			SimplePath simplePath = BgpNode.toSimplePath(state);
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
					SimplePath simplePath = BgpNode.toSimplePath(state);
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

	public static String toString(HLFacetConstraint<?> constraint, LookupService<Node, String> labelService) {
		Expr expr = constraint.expr();
		Set<Node> nodes = extractNodes(constraint);
		
		Map<Node, String> nodeToLabel = getLabels(nodes, Function.identity(), labelService);
		
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
	
	
	//RDFConnection conn;
	FacetedQuery fq;
	LookupService<Node, String> labelService;

	ActionListBox facetList = new ActionListBox(); //new TerminalSize(30, 10));
	CheckBoxList<FacetValueCount> facetValueList = new CheckBoxList<>();
	Table<String> resultTable = new Table<String>("Item");

	CheckBoxList<HLFacetConstraint<?>> constraintList = new CheckBoxList<>();

	String facetFilter = null;
	
	FacetDirNode fdn;
	Node selectedFacet = null;
	
	boolean includeAbsent = true;
	
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
			constraintList.addItem(hlc);
		}
	}
	
	public void updateItems(FacetedQuery fq) {
		List<RDFNode> items = fq.focus().availableValues().exec().toList().blockingGet();
		
		 TableModel<String> model = resultTable.getTableModel();
		model.clear();
		
		for(RDFNode item : items) {
			model.addRow("" + item);
		}
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
	
	
	public void updateFacetValues() {
		if(fdn != null && selectedFacet != null) {
			
			
			facetValueList.setEnabled(false);
			List<FacetValueCount> fvcs = fdn.facetValueCountsWithAbsent(includeAbsent).only(selectedFacet).exec().toList().blockingGet();
	
			System.out.println("Got facet values:\n" + fvcs.stream().map(x -> x.getValue()).collect(Collectors.toList()));
			
			enrichWithLabels(fvcs, FacetValueCount::getValue, labelService);
			
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
		}
	}
	
	public void updateFacets(FacetedQuery fq) {
		
		if(fdn != null) {
		
			facetList.setEnabled(false);
		
			UnaryRelation filter = facetFilter == null ? null : KeywordSearchUtils.createConceptRegexIncludeSubject(BinaryRelationImpl.create(RDFS.label), facetFilter);
	
					
			List<FacetCount> fcs = fdn.facetCounts(includeAbsent)
					.filter(filter)
					.exec().toList()
					// TODO This is still not idiomatic / we want to have a flow where we can cancel lable lookup
					.doOnSuccess(list -> enrichWithLabels(list, FacetCount::getPredicate, labelService))
					.blockingGet();
			//enrichWithLabels(fcs, FacetCount::getPredicate, labelService);
		
			//List<Entry<FacetDirNode, >>fcs.stream().map(fc -> Maps.immutableEntry(fdn, fc)).collect(Collectors.toList());
			facetList.clearItems();
			
			
			for(FacetCount fc : fcs) {
					facetList.addItem(fc.getProperty(RDFS.label).getString() + " (" + fc.getDistinctValueCount().getCount() + ")" , new Runnable() {
					@Override
					public void run() {
						selectFacet(fdn, fc.getPredicate());
						
		//				fc.getPredicate();
		//				fc.g
		//				fdn.via(node)
						
	//					System.out.println("run");
						// Code to run when action activated
					}
				});
			}
			
			facetList.setEnabled(true);
		}
	}

	
	public static void main(String[] args) throws Exception {
		
	    OptionParser parser = new OptionParser();

	    NonOptionArgumentSpec<String> filesOs = parser
	    		.nonOptions()
	    		.describedAs("Input files");

	    
	    OptionSet optionSet = parser.parse(args);
	    

	    List<String> files = filesOs.values(optionSet);
	    
	    Model model = ModelFactory.createDefaultModel();
	    for(String file : files) {
	    	Model tmp = RDFDataMgr.loadModel(file);
	    	model.add(tmp);
	    }
	    
	    Dataset dataset = DatasetFactory.wrap(model);
		
		new MainCliFacete3().init(dataset);
	}
	
	
	public void setFacetDir(org.aksw.facete.v3.api.Direction dir) {
		fdn = fdn.parent().step(dir);
		updateFacets(fq);
	}
	
	public void init(Dataset dataset) throws Exception
	{
		
		//Dataset dataset = RDFDataMgr.loadDataset("/home/raven/.dcat/repository/datasets/data/dcat.linkedgeodata.org/dataset/osm-bremen-2018-04-04/_content/dcat.ttl");
//		Dataset dataset = RDFDataMgr.loadDataset("path-data-simple.ttl");
		RDFConnection conn = RDFConnectionFactory.connect(dataset);
		
		fq = FacetedQueryImpl.create(conn);

		//facetList.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		facetList.setInputFilter((i, keyStroke) -> {
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
				.createLookupService(new QueryExecutionFactorySparqlQueryConnection(fq.connection()), BinaryRelationImpl.create(RDFS.label))
				.mapValues((k, vs) -> vs.isEmpty() ? deriveLabelFromIri(k.getURI()) : vs.iterator().next())
				.mapValues((k, v) -> "" + v);

		
		// Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().setTerminalEmulatorTitle("Facete III").setMouseCaptureMode(MouseCaptureMode.CLICK).createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        
        WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

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
		
		
		facetValueList.addListener((int itemIndex, boolean checked) -> {
			FacetValueCount item = facetValueList.getItemAt(itemIndex);
			//System.out.println(item);

			HLFacetConstraint<? extends ConstraintFacade<? extends FacetNode>> tmp = fdn.via(item.getPredicate()).one().constraints().eq(item.getValue());
			tmp.setActive(checked);
			
			
			updateFacets(fq);
			updateItems(fq);
			updateConstraints(fq);

		});

		facetValueList.setListItemRenderer(new CheckBoxList.CheckBoxListItemRenderer<FacetValueCount>() {
			@Override
			public String getLabel(com.googlecode.lanterna.gui2.CheckBoxList<FacetValueCount> listBox, int index, FacetValueCount item) {
	            boolean checked = listBox.isChecked(index);
	            String check = checked ? "x" : " ";

	            
	            
	            String text = Optional.ofNullable(item.getProperty(RDFS.label)).map(Statement::getString).orElse("(null)") + " (" + item.getFocusCount().getCount() + ")"; //item.toString();
	            return "[" + check + "] " + text;
			};
		});

//		FacetDirNode fdn2 = fq.focus().fwd();

		updateFacetValues();
		
		

		

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

		
		Button btnApply = new Button("Ok") {
			public synchronized com.googlecode.lanterna.gui2.Interactable.Result handleKeyStroke(KeyStroke keyStroke) {
				if(keyStroke.getKeyType().equals(KeyType.Enter)) {
					MessageDialog.showMessageDialog(textGUI, "test", "test");
				}
				return super.handleKeyStroke(keyStroke);
			};
		};
		
		
		
		Panel facetPathPanel = new Panel();
				//GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING, true, true, 2, 1)));
		
		Panel facetValuePanel = new Panel();
				
		// Prevent focus change on down arrow key when at end of list 
		facetValueList.setInputFilter((i, keyStroke) -> {
			boolean r;
			
			if(KeyType.ArrowLeft.equals(keyStroke.getKeyType())) {
				facetList.takeFocus();
			}
			
			r = !(keyStroke.getKeyType().equals(KeyType.ArrowUp) && facetValueList.getSelectedIndex() == 0) &&
			!(keyStroke.getKeyType().equals(KeyType.ArrowDown) && facetValueList.getItems().size() - 1 == facetValueList.getSelectedIndex());
			return r;
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
			public String getLabel(com.googlecode.lanterna.gui2.CheckBoxList<HLFacetConstraint<?>> listBox, int index, HLFacetConstraint<?> item) {
	            boolean checked = listBox.isChecked(index);
	            String check = checked ? "x" : " ";

	            String text = MainCliFacete3.toString(item, labelService);
	            return "[" + check + "] " + text;
			};
		});


		Panel facetPanel = new Panel();
		Panel constraintPanel = new Panel();
		Panel resultPanel = new Panel();

		// Component hierarchy and layouts
		

		facetPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1)); //.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));
		facetPanel.setLayoutManager(new GridLayout2(1));
		facetPanel.addComponent(facetFilterPanel.withBorder(Borders.singleLine("Filter")));
		facetPanel.addComponent(facetPathPanel);
		facetPanel.addComponent(facetList);

		facetFilterBox.setLayoutData(GridLayout2.createHorizontallyFilledLayoutData(1));

		facetFilterPanel.setLayoutData(GridLayout2.createHorizontallyFilledLayoutData(1));
		facetFilterPanel.setLayoutManager(new GridLayout2(2));
		facetFilterPanel.addComponent(facetFilterBox);
		facetFilterPanel.addComponent(btnApply);
		facetFilterPanel.addComponent(btnApply);

		facetPathPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.BEGINNING, Alignment.CENTER, true, false, 1, 1)); //GridLayout.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
		facetPathPanel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));
		facetPathPanel.addComponent(new Button("Foo"));
		facetPathPanel.addComponent(new Button("<", () -> setFacetDir(org.aksw.facete.v3.api.Direction.BACKWARD)));
		facetPathPanel.addComponent(new Button(">", () -> setFacetDir(org.aksw.facete.v3.api.Direction.FORWARD)));


		facetValuePanel.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
		facetValuePanel.setLayoutManager(new GridLayout2(1));
		facetValuePanel.addComponent(facetValueList);


		facetList.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, false, 1, 1));
		facetValueList.setLayoutData(GridLayout2.createHorizontallyFilledLayoutData(1));
		constraintList.setLayoutData(GridLayout2.createHorizontallyFilledLayoutData(1));

		constraintPanel.setLayoutData(GridLayout2.createLayoutData(GridLayout2.Alignment.FILL, GridLayout2.Alignment.BEGINNING, true, false, 2, 1));
		constraintPanel.addComponent(constraintList);


		resultTable.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, true, 1, 1));


		resultPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.BEGINNING, true, true, 2, 1));
		resultPanel.setLayoutManager(new GridLayout2(1));
		resultPanel.addComponent(resultTable);
		




		//mainPanel.setLayoutData(GridLayout2.createLayoutData(Alignment.FILL, Alignment.FILL, true, true, 2, 1));
		mainPanel.setLayoutManager(new GridLayout2(2));
		mainPanel.addComponent(facetPanel.withBorder(Borders.singleLine("Facets")));
		mainPanel.addComponent(facetValuePanel.withBorder(Borders.singleLine("Facet Values")));
		mainPanel.addComponent(constraintPanel.withBorder(Borders.singleLine("Constraints")));
		mainPanel.addComponent(resultPanel.withBorder(Borders.singleLine("Matches")));

		
		
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
		
        //window.setHints(Collections.singleton(Window.Hint.NO_POST_RENDERING));

        window.setHints(Arrays.asList(Window.Hint.NO_POST_RENDERING, Window.Hint.EXPANDED, Window.Hint.FIT_TERMINAL_WINDOW));

        gui.addWindowAndWait(window);

	}
	
//	public static void enrichWithLabels(Collection<FacetValueCount> cs, LookupService<Node, String> labelService) {
//		Map<Node, FacetCount> index = Maps.uniqueIndex(cs, FacetCount::getPredicate);		
//		Map<Node, String> map = labelService.fetchMap(index.keySet());
//		index.forEach((k, v) -> v.addLiteral(RDFS.label, map.getOrDefault(k, k.getLocalName())));
//	}

	public static <T extends Resource> void enrichWithLabels(Collection<T> cs, Function<? super T, ? extends Node> nodeFunction, LookupService<Node, String> labelService) {
		// Replace null nodes with Node.NULL
		// TODO Use own own constant should jena remove this deprecated symbol
		Multimap<Node, T> index = Multimaps.index(cs, item ->
			Optional.<Node>ofNullable(nodeFunction.apply(item)).orElse(ConstraintFacadeImpl.N_ABSENT));

		//Map<Node, T> index = Maps.uniqueIndex();
		Set<Node> s = index.keySet().stream()
				.filter(Node::isURI)
				.collect(Collectors.toSet());

		Map<Node, String> map = labelService.fetchMap(s);
		index.forEach((k, v) -> v.addLiteral(RDFS.label,
				map.getOrDefault(k, ConstraintFacadeImpl.N_ABSENT.equals(k)
						? "(null)"
						: k.isURI() ? deriveLabelFromIri(k.getURI()) : k.toString())));
	}

	public static String deriveLabelFromIri(String iriStr) {

		String result;
		for(;;) {
			// This is what Node.getLocalName does
			result = iriStr.substring( Util.splitNamespaceXML(iriStr));
			if(result.isEmpty() && !iriStr.isEmpty()) {
				iriStr = iriStr.substring(0, iriStr.length() - 1);
				continue;
			} else {
				break;
			}
		};	
		return result;
	}
	

	public static <T> Map<T, String> getLabels(Collection<T> cs, Function<? super T, ? extends Node> nodeFunction, LookupService<Node, String> labelService) {
		Multimap<Node, T> index = Multimaps.index(cs, nodeFunction::apply);
		//Map<Node, T> index = Maps.uniqueIndex();
		Set<Node> s = index.keySet().stream().filter(Node::isURI).collect(Collectors.toSet());
		Map<Node, String> map = labelService.fetchMap(s);
		
		//Map<T, String> result = n

		Function<Node, String> determineLabel = k -> map.getOrDefault(k, k.isURI() ? deriveLabelFromIri(k.getURI()) : k.toString()); 
		
		Map<T, String> result =
			index.entries().stream().map(
			e -> Maps.immutableEntry(e.getValue(), determineLabel.apply(e.getKey())))
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		return result;
		//index.forEach((k, v) -> v.addLiteral(RDFS.label, map.getOrDefault(k, k.isURI() ? k.getLocalName() : k.toString())));
	}

}
