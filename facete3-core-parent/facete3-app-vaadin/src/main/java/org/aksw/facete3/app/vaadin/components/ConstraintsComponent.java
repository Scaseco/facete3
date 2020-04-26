package org.aksw.facete3.app.vaadin.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.base.Joiner;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import org.aksw.facete.v3.api.FacetConstraint;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetedQuery;
import org.aksw.facete.v3.api.FacetedQueryResource;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.impl.FacetNodeImpl;
import org.aksw.facete.v3.impl.HLFacetConstraintImpl;
import org.aksw.facete3.app.vaadin.MainView;
import org.aksw.facete3.app.vaadin.QueryConf;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;


public class ConstraintsComponent extends VerticalLayout {

    private QueryConf queryConf;
    private MainView mainView;

    public ConstraintsComponent(MainView mainView, QueryConf queryConf) {

        this.queryConf = queryConf;
        this.mainView = mainView;
        // refresh();
    }
    // 
    // public void refresh() {
    //     List<HLFacetConstraint<?>> constraints = getItems();
    //     Grid<HLFacetConstraint<?>> grid = new Grid<>();
    //     grid.setItems(constraints);
    // }
    // 
    // private List<HLFacetConstraint<?>> getItems() {
    // 
    //     List<HLFacetConstraint<?>> constraints = new ArrayList<HLFacetConstraint<?>>();
    //     FacetedQuery facetedQuery = queryConf.getFacetedQuery();
    //     for (FacetConstraint c : facetedQuery.constraints()) {
    //         HLFacetConstraint<?> hlc = toHlConstraint(facetedQuery, c);
    //         // TODO We should add pairs with the facet constraints together with the
    //         // precomputed string
    //         // then we can batch the label lookups here
    //         constraints.add(hlc);
    //     }
    //     return constraints;
    // }
    // 
    // private HLFacetConstraint<?> toHlConstraint(FacetedQuery facetedQuery, FacetConstraint facetConstraint) {
    // 
    //     FacetedQueryResource r = facetedQuery.as(FacetedQueryResource.class);
    //     // HACK FacetNodeImpl requires a bgpNode - but we don't need its value
    //     // We only need it in order to set up HLFacetConstraint.pathsMentioned
    //     FacetNode tmp = new FacetNodeImpl(r, HACK);
    // 
    //     HLFacetConstraint<?> result = new HLFacetConstraintImpl<Void>(null, tmp, facetConstraint);
    //     return result;
    // }
    // 
    // public static BgpNode HACK = ModelFactory.createDefaultModel().createResource("should not appear anywhere")
    //         .as(BgpNode.class);
    // 
    //    public static String toString(
    //         HLFacetConstraint<?> constraint,
    //         LookupService<Node, String> labelService,
    //         PrefixMapping prefixes) {
    //     Expr expr = constraint.expr();
    //     Set<Node> nodes = extractNodes(constraint);
    // 
    //     Map<Node, String> nodeToLabel = getLabels(nodes, Function.identity(), labelService, prefixes);
    // 
    //     Map<Node, String> bgpNodeLabels = indexPaths(constraint).entrySet().stream()
    //         .collect(Collectors.toMap(Entry::getKey, e -> toString(e.getValue(), nodeToLabel::get)));
    // 
    //     // Combine the maps to get the final label mapping
    //     nodeToLabel.putAll(bgpNodeLabels);
    // 
    //     String result = toString(expr, nodeToLabel::get);
    // 
    //     return result;
    // }
    //  public static String toString(Expr expr, Function<? super Node, ? extends String> nodeToLabel) {
    //     String result;
    //     if(expr.isConstant()) {
    //         Node node = expr.getConstant().asNode();
    //         result = nodeToLabel.apply(node);
    //     } else if(expr.isVariable()) {
    //         Node node = expr.asVar();
    //         result = nodeToLabel.apply(node);
    //     } else {
    //         ExprFunction f = expr.getFunction();
    //         String symbol = f.getFunctionSymbol().getSymbol();
    //         if(symbol == null || symbol.isEmpty()) {
    //             symbol = f.getFunctionIRI();
    //         }
    // 
    //         List<String> argStrs = f.getArgs().stream()
    //                 .map(e -> toString(e, nodeToLabel))
    //                 .collect(Collectors.toList());
    // 
    //         result =
    //                 argStrs.size() == 1 ? symbol + argStrs.iterator().next() :
    //                 argStrs.size() == 2 ? argStrs.get(0) + " " + symbol + " " + argStrs.get(1) :
    //                 symbol + "(" + Joiner.on(",").join(argStrs) + ")";
    //     }
    // 
    //     return result;
    // }
}
