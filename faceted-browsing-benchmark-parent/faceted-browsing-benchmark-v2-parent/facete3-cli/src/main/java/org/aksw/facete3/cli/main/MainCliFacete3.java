package org.aksw.facete3.cli.main;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.facete.v3.api.FacetCount;
import org.aksw.facete.v3.api.FacetValueCount;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.impl.FacetedQueryImpl;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.core.connection.QueryExecutionFactorySparqlQueryConnection;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.apache.jena.ext.com.google.common.collect.Maps;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDFS;

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
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowListener;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

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
	public static void main(String[] args) throws IOException {
		
		
		Dataset dataset = RDFDataMgr.loadDataset("path-data-simple.ttl");
		RDFConnection conn = RDFConnectionFactory.connect(dataset);
		
		FacetedQuery fq = FacetedQueryImpl.create(conn);

		
		// Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();
        
//		TerminalSize size = new TerminalSize(80, 25);
		
		Panel mainPanel = new Panel();
		mainPanel.setLayoutManager(new GridLayout(2)); //Direction.HORIZONTAL));
		
		
		TextBox facetFilterBox = new TextBox(); //new TerminalSize(30, 1));
		
		ActionListBox facetList = new ActionListBox(); //new TerminalSize(30, 10));

		CheckBoxList<String> facetValueList = new CheckBoxList<>(); //new TerminalSize(30, 10));
		//facetValueList.getRenderer().p

		LookupService<Node, String> labelService = LookupServiceUtils
					.createLookupService(new QueryExecutionFactorySparqlQueryConnection(conn), BinaryRelationImpl.create(RDFS.label))
					.mapValues((k, vs) -> vs.isEmpty() ? k.getLocalName() : vs.iterator().next())
					.mapValues((k, v) -> "" + v);
		
		List<FacetCount> fcs = fq.focus().fwd().facetCounts().exec().toList().blockingGet();
		enrichWithLabels(fcs, FacetCount::getPredicate, labelService);
		
		
		for(FacetCount fc : fcs) {
				facetList.addItem(fc.getProperty(RDFS.label).getString() + " (" + fc.getDistinctValueCount().getCount() + ")" , new Runnable() {
				@Override
				public void run() {
					System.out.println("run");
					// Code to run when action activated
				}
			});
		}

		
		List<FacetValueCount> fvcs = fq.focus().fwd().facetValueCounts().exec().toList().blockingGet();
		enrichWithLabels(fvcs, FacetValueCount::getValue, labelService);
		for(FacetValueCount fvc : fvcs) {
			facetValueList.addItem(fvc.getProperty(RDFS.label).getString() + " (" + fvc.getFocusCount().getCount() + ")");
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
		
		Panel facetFilterPanel = new Panel(new GridLayout(2));
		facetFilterPanel.addComponent(facetFilterBox);
		facetFilterPanel.addComponent(new Button("Apply"));
		
		
		facetPanel.addComponent(facetFilterPanel.withBorder(Borders.singleLine("Filter")));

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

		Panel facetValuePanel = new Panel(new GridLayout(2));

		// Prevent focus change on down arrow key when at end of list 
		facetValueList.setInputFilter((i, k) -> 
		!(k.getKeyType().equals(KeyType.ArrowUp) && facetValueList.getSelectedIndex() == 0) &&
		!(k.getKeyType().equals(KeyType.ArrowDown) && facetValueList.getItems().size() - 1 == facetValueList.getSelectedIndex()));
		facetValuePanel.addComponent(facetValueList);
		
		mainPanel.addComponent(facetPanel.withBorder(Borders.singleLine("Facets")));

		mainPanel.addComponent(facetValuePanel.withBorder(Borders.singleLine("Facet Values")));
		
		
		Table<String> resultTable = new Table<String>("", "Column 2", "Column 3");
		
		Panel resultPanel = new Panel(new GridLayout(1));
		resultPanel.addComponent(resultTable.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING, true, true, 1, 1)));
		resultTable.getTableModel().addRow("[x]", "b", "c");
		//resultPanel;
		
		//mainPanel.addComponent(resultPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING, true, true, 2, 1)).withBorder(Borders.singleLine("Items")));

		mainPanel.addComponent(resultPanel.setLayoutData(GridLayout.createLayoutData(GridLayout.Alignment.BEGINNING, GridLayout.Alignment.BEGINNING, true, true, 2, 1)).withBorder(Borders.singleLine("Items")));

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
        //window.setHints(Collections.singleton(Window.Hint.NO_POST_RENDERING));

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
        gui.addWindowAndWait(window);

	}
	
//	public static void enrichWithLabels(Collection<FacetValueCount> cs, LookupService<Node, String> labelService) {
//		Map<Node, FacetCount> index = Maps.uniqueIndex(cs, FacetCount::getPredicate);		
//		Map<Node, String> map = labelService.fetchMap(index.keySet());
//		index.forEach((k, v) -> v.addLiteral(RDFS.label, map.getOrDefault(k, k.getLocalName())));
//	}

	public static <T extends Resource> void enrichWithLabels(Collection<T> cs, Function<? super T, ? extends Node> nodeFunction, LookupService<Node, String> labelService) {
		Map<Node, T> index = Maps.uniqueIndex(cs, nodeFunction::apply);
		Set<Node> s = index.keySet().stream().filter(Node::isURI).collect(Collectors.toSet());
		Map<Node, String> map = labelService.fetchMap(s);
		index.forEach((k, v) -> v.addLiteral(RDFS.label, map.getOrDefault(k, k.isURI() ? k.getLocalName() : k.toString())));
	}
}
