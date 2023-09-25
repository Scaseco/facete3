package org.aksw.facete3.app.shared.label;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.commons.rx.lookup.LookupService;
import org.aksw.facete.v3.api.FacetNode;
import org.aksw.facete.v3.api.FacetNodeResource;
import org.aksw.facete.v3.api.HLFacetConstraint;
import org.aksw.facete.v3.bgp.api.BgpNode;
import org.aksw.facete.v3.bgp.impl.BgpNodeUtils;
import org.aksw.jenax.dataaccess.LabelUtils;
import org.aksw.jenax.sparql.path.SimplePath;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.common.graph.Traverser;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprFunction;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.path.P_Path0;

public class FaceteLabelUtils {
      public static Map<Node, SimplePath> indexPaths(HLFacetConstraint<?> constraint) {
          Map<Node, SimplePath> result = constraint.mentionedFacetNodes()
                  .entrySet()
                  .stream()
                  .map(e -> {
                      Node k = e.getKey();
                      FacetNode fn = e.getValue();
                      BgpNode state = fn.as(FacetNodeResource.class)
                              .state();
                      SimplePath simplePath = BgpNodeUtils.toSimplePath(state);
                      return Maps.immutableEntry(k, simplePath);
                  })
                  .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
      return result;
    }

    public static Set<Node> extractNodes(
            HLFacetConstraint<?> constraint
            ) {

        Map<Node, FacetNode> map = constraint.mentionedFacetNodes();

        Set<Node> nodes = new LinkedHashSet<>();

        for (FacetNode fn : map.values()) {
            BgpNode state = fn.as(FacetNodeResource.class).state();
            SimplePath simplePath = BgpNodeUtils.toSimplePath(state);
            Set<Node> contrib = SimplePath.mentionedNodes(simplePath);

            nodes.addAll(contrib);
        }

        // Add all iri and literal constants of the expr to the result
        Expr expr = constraint.expr();
        Set<Node> contrib = Streams
                .stream(Traverser
                        .<Expr>forTree(e -> e.isFunction()
                                ? e.getFunction().getArgs()
                                : Collections.emptyList())
                        .depthFirstPreOrder(expr))
                .filter(Expr::isConstant).map(Expr::getConstant).map(NodeValue::asNode)
                .filter(n -> n.isURI() || n.isLiteral()).collect(Collectors.toSet());

        nodes.addAll(contrib);

        return nodes;
    }


    public static String toString(HLFacetConstraint<?> constraint, Map<Node, String> nodeToLabel) {
        Expr expr = constraint.expr();

        Map<Node, String> bgpNodeLabels = indexPaths(constraint).entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> toString(e.getValue(), nodeToLabel::get)));

        // Combine the maps to get the final label mapping
        nodeToLabel.putAll(bgpNodeLabels);

        String result = toString(expr, nodeToLabel::get);

        return result;
    }

    public static String toString(HLFacetConstraint<?> constraint,
            LookupService<Node, String> labelService) {
        Expr expr = constraint.expr();
        Set<Node> nodes = extractNodes(constraint);

        Map<Node, String> nodeToLabel = LabelUtils.getLabels(nodes, Function.identity(), labelService);

        Map<Node, String> bgpNodeLabels = indexPaths(constraint).entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, e -> toString(e.getValue(), nodeToLabel::get)));

        // Combine the maps to get the final label mapping
        nodeToLabel.putAll(bgpNodeLabels);

        String result = toString(expr, nodeToLabel::get);

        return result;
    }

    public static String toString(SimplePath sp, Function<Node, String> nodeToStr) {
      String result = sp.getSteps().stream()
          .map(step -> toString(step, nodeToStr))
          .collect(Collectors.joining("/"));

      return result;
    }

    public static String toString(P_Path0 step, Function<Node, String> nodeToStr) {
        Node node = step.getNode();
        String result = (!step.isForward() ? "^" : "") + nodeToStr.apply(node);
        return result;
    }

  public static String toString(Expr expr, Function<? super Node, ? extends String> nodeToLabel) {
      String result;
      if (expr.isConstant()) {
          Node node = expr.getConstant()
                  .asNode();
          result = nodeToLabel.apply(node);
      } else if (expr.isVariable()) {
          Node node = expr.asVar();
          result = nodeToLabel.apply(node);
      } else {
          ExprFunction f = expr.getFunction();
          String symbol = f.getFunctionSymbol()
                  .getSymbol();
          if (symbol == null || symbol.isEmpty()) {
              symbol = f.getFunctionIRI();
          }

          symbol = prettifySymbol(symbol);

          List<String> argStrs = f.getArgs()
                  .stream()
                  .map(e -> toString(e, nodeToLabel))
                  .collect(Collectors.toList());

          result = argStrs.size() == 1 ? symbol + argStrs.iterator()
                  .next()
                  : argStrs.size() == 2 ? argStrs.get(0) + " " + symbol + " " + argStrs.get(1)
                          : symbol + "(" + Joiner.on(",")
                                  .join(argStrs) + ")";
  }

  return result;
}

    public static String prettifySymbol(String symbol) {
        Map<String, String> map = new HashMap<>();
        map.put("eq", "is");
        return map.getOrDefault(symbol, symbol);
    }

}
