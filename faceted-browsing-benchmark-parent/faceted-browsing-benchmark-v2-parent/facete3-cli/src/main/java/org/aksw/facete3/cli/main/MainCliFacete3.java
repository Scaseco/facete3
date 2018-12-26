package org.aksw.facete3.cli.main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.ConstraintFacade;
import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetDirNode;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Borders;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.CheckBoxList;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
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


// If we wanted to create an Amazon like faceted interface we'd need:
// Ranking function over a set of values - rank(concept) -> BinaryRelation (resource, rank-value)
// Rank is actually just a 'combine'* with a ranking attribute
// We can then sort the binary relation by rank
// binaryRelation.clearOrder(); binaryRelation.addOrder(binaryRelation.getTargetVar(), 1)
// * my current term for the generalization of a sparql join on the syntax level - which isn't necessarily a join in the first place


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
	
	//RDFConnection conn;
	FacetedQuery fq;
	LookupService<Node, String> labelService;

	Table<String> resultTable = new Table<String>("Item");
	ActionListBox facetList = new ActionListBox(); //new TerminalSize(30, 10));

	
	
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
	
	public void updateItems(FacetedQuery fq) {
		List<RDFNode> items = fq.focus().availableValues().exec().toList().blockingGet();
		
		 TableModel<String> model = resultTable.getTableModel();
		model.clear();
		
		for(RDFNode item : items) {
			model.addRow("" + item);
		}
	}
	
	public void updateFacets(FacetedQuery fq) {
		
		facetList.setEnabled(false);
	
		FacetDirNode fdn = fq.focus().fwd();
		List<FacetCount> fcs = fdn.facetCounts().exec().toList()
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
	//				fc.getPredicate();
	//				fc.g
	//				fdn.via(node)
					
					System.out.println("run");
					// Code to run when action activated
				}
			});
		}
		
		facetList.setEnabled(true);
	}

	
	public static void main(String[] args) throws Exception {
		new MainCliFacete3().init();
	}
	
	
	public void init() throws Exception
	{
		Dataset dataset = RDFDataMgr.loadDataset("path-data-simple.ttl");
		RDFConnection conn = RDFConnectionFactory.connect(dataset);
		
		fq = FacetedQueryImpl.create(conn);

		facetList.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		
		labelService = LookupServiceUtils
				.createLookupService(new QueryExecutionFactorySparqlQueryConnection(fq.connection()), BinaryRelationImpl.create(RDFS.label))
				.mapValues((k, vs) -> vs.isEmpty() ? k.getLocalName() : vs.iterator().next())
				.mapValues((k, v) -> "" + v);

		
		// Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().setMouseCaptureMode(MouseCaptureMode.CLICK).createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        
        WindowBasedTextGUI textGUI = new MultiWindowTextGUI(screen);

//		TerminalSize size = new TerminalSize(80, 25);
		
		Panel mainPanel = new Panel(new GridLayout(2));
		//mainPanel.setLayoutManager(new GridLayout(2)); //Direction.HORIZONTAL));
		
		
		TextBox facetFilterBox = new TextBox(); // new TerminalSize(16, 1))
		
		updateFacets(fq);
		updateItems(fq);
		
		
		CheckBoxList<FacetValueCount> facetValueList = new CheckBoxList<>();
		facetValueList.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));
		facetValueList.setListItemRenderer(new CheckBoxList.CheckBoxListItemRenderer<FacetValueCount>() {
			@Override
			public String getLabel(com.googlecode.lanterna.gui2.CheckBoxList<FacetValueCount> listBox, int index, FacetValueCount item) {
	            boolean checked = listBox.isChecked(index);
	            String check = checked ? "x" : " ";

	            String text = item.getProperty(RDFS.label).getString() + " (" + item.getFocusCount().getCount() + ")"; //item.toString();
	            return "[" + check + "] " + text;
			};
		});

		FacetDirNode fdn2 = fq.focus().fwd();
		List<FacetValueCount> fvcs = fdn2.facetValueCounts().exec().toList().blockingGet();

		facetValueList.addListener((int itemIndex, boolean checked) -> {
			FacetValueCount item = facetValueList.getItemAt(itemIndex);
			System.out.println(item);

			HLFacetConstraint<? extends ConstraintFacade<? extends FacetNode>> tmp = fdn2.via(item.getPredicate()).one().constraints().eq(item.getValue());
			tmp.setActive(checked);
			
			
			updateFacets(fq);
			updateItems(fq);

		});

		
		enrichWithLabels(fvcs, FacetValueCount::getValue, labelService);
		for(FacetValueCount fvc : fvcs) {
			facetValueList.addItem(fvc);
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

		
		Panel facetPanel = new Panel(new GridLayout(1));
		facetPanel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));

		Panel facetFilterPanel = new Panel(new GridLayout(3));
		facetFilterPanel.addComponent(facetFilterBox);
		
		Button btnClear = new Button("Clr") {
			public synchronized com.googlecode.lanterna.gui2.Interactable.Result handleKeyStroke(KeyStroke keyStroke) {
				if(keyStroke.getKeyType().equals(KeyType.Enter)) {
					facetFilterBox.setText("");
				}
				return super.handleKeyStroke(keyStroke);
			};
		};
		facetFilterPanel.addComponent(btnClear);

		
		Button btnApply = new Button("Ok") {
			public synchronized com.googlecode.lanterna.gui2.Interactable.Result handleKeyStroke(KeyStroke keyStroke) {
				if(keyStroke.getKeyType().equals(KeyType.Enter)) {
					MessageDialog.showMessageDialog(textGUI, "test", "test");
				}
				return super.handleKeyStroke(keyStroke);
			};
		};
		facetFilterPanel.addComponent(btnApply);
		
		
		facetPanel.addComponent(facetFilterPanel.withBorder(Borders.singleLine("Filter")));
//		facetPanel.addComponent(facetFilterPanel);

		//mainPanel.withBorder(Borders.singleLine("Facete 3"));

//		facetPanel.addComponent(facetFilterBox.setLayoutData(
//				GridLayout.createLayoutData(
//					GridLayout.Alignment.BEGINNING,
//					GridLayout.Alignment.BEGINNING,
//					true,
//					false,
//					1, 1
//					)));

		facetPanel.addComponent(facetList);//.setLayoutData(
				//GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING, true, true, 2, 1)));
		
		//mainPanel.addComponent(new Label("Type:"));

		Panel facetValuePanel = new Panel(new GridLayout(1));
		facetValuePanel.setLayoutData(GridLayout.createHorizontallyFilledLayoutData(1));//.createHorizontallyEndAlignedLayoutData(1));
		
		// Prevent focus change on down arrow key when at end of list 
		facetValueList.setInputFilter((i, k) -> 
		!(k.getKeyType().equals(KeyType.ArrowUp) && facetValueList.getSelectedIndex() == 0) &&
		!(k.getKeyType().equals(KeyType.ArrowDown) && facetValueList.getItems().size() - 1 == facetValueList.getSelectedIndex()));
		facetValuePanel.addComponent(facetValueList);
		
		mainPanel.addComponent(facetPanel.withBorder(Borders.singleLine("Facets")));
		//mainPanel.addComponent(facetPanel);

		mainPanel.addComponent(facetValuePanel.withBorder(Borders.singleLine("Facet Values")));
		
		
		
		Panel resultPanel = new Panel(new GridLayout(1));
		resultPanel.addComponent(resultTable.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING, true, true, 1, 1)));
		//resultTable.getTableModel().addRow("[x]", "b", "c");
		//resultPanel;
		
		//mainPanel.addComponent(resultPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING, true, true, 2, 1)).withBorder(Borders.singleLine("Items")));

//		mainPanel.addComponent(resultPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING, true, true, 2, 1)).withBorder(Borders.singleLine("Items")));
		
		
//XXX		mainPanel.addComponent(resultPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING, true, true, 2, 1)));

		//mainPanel.addComponent(new EmptySpace());
		//mainPanel.addComponent(facetValueList);
		
//		CheckBoxList<String> checkBoxList = new CheckBoxList<String>(size);
//		checkBoxList.addItem("item 1");
//		checkBoxList.addItem("item 2");
//		checkBoxList.addItem("item 3");
//
//		mainPanel.addComponent(checkBoxList);

		
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
		Multimap<Node, T> index = Multimaps.index(cs, nodeFunction::apply);
		//Map<Node, T> index = Maps.uniqueIndex();
		Set<Node> s = index.keySet().stream().filter(Node::isURI).collect(Collectors.toSet());
		Map<Node, String> map = labelService.fetchMap(s);
		index.forEach((k, v) -> v.addLiteral(RDFS.label, map.getOrDefault(k, k.isURI() ? k.getLocalName() : k.toString())));
	}
}
