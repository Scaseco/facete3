package org.aksw.facete3.app.vaadin;

//public class LabelService {
//
//    private RDFConnection connection;
//    private PrefixMapping prefixes;
//    private LookupService<Node, String> labelLookupService;
//    private Property labelProperty;
//
//
//    public LabelService(RDFConnection connection, Property labelProp) {
//        labelProperty = labelProp;
//        initLabelService(connection);
//
//    }
//
//    public LabelService(RDFConnection connection) {
//        labelProperty = RDFS.label;
//        initLabelService(connection);
//
//    }
//
//    private void initLabelService(RDFConnection connection) {
//        this.connection = connection;
//        prefixes = new PrefixMappingImpl();
//        labelLookupService = getLabelLookupService();
//    }
//
//    private LookupService<Node, String> getLabelLookupService() {
//        BinaryRelation labelRelation = BinaryRelationImpl.create(labelProperty);
//        return LookupServiceUtils.createLookupService(connection, labelRelation)
//                .partition(10)
//                .cache()
//                .mapValues(this::getLabelFromLookup);
//    }
//
//    private String getLabelFromLookup(Node node, List<Node> results) {
//        String label = "";
//        if (!results.isEmpty()) {
//            Node labelNode = results.get(0);
//            label = labelNode.toString();
//        } else {
//            String uri = node.getURI();
//            label = deriveLabelFromIri(uri);
//        }
//        return label;
//    }
//
//    public <T extends RDFNode> void enrichWithLabels(Collection<T> rdfNodes,
//            Function<? super T, ? extends Node> defineNodeForLabelFunction) {
//
//        Property labelProperty = RDFS.label;
//
//        Multimap<Node, T> index = Multimaps.index(rdfNodes, defineNodeForLabelFunction::apply);
//        Set<Node> s = index.keySet()
//                .stream()
//                .filter(Node::isURI)
//                .collect(Collectors.toSet());
//        Map<Node, String> map = labelLookupService.fetchMap(s);
//        index.forEach((k, v) -> v.asResource()
//                .addLiteral(labelProperty,
//                        map.getOrDefault(k,
//                                NodeUtils.nullUriNode.equals(k) ? "(null)"
//                                        : k.isURI() ? deriveLabelFromIri(k.getURI())
//                                                : formatLiteralNode(k, prefixes))));
//    }
////
////    public static <T> Map<T, String> getLabels(Collection<T> rdfNodes,
////            Function<? super T, ? extends Node> defineNodeForLabelFunction) {
////        Multimap<Node, T> index = Multimaps.index(rdfNodes, defineNodeForLabelFunction::apply);
////        Set<Node> labelNodes = index.keySet()
////                .stream()
////                .filter(Node::isURI)
////                .collect(Collectors.toSet());
////        Map<Node, String> map = labelLookupService.fetchMap(labelNodes);
////
////        Function<Node, String> determineLabel = k -> map.getOrDefault(k,
////                k.isURI() ? deriveLabelFromIri(k.getURI()) : formatLiteralNode(k, prefixes));
////
////        Map<T, String> result = index.entries()
////                .stream()
////                .map(e -> Maps.immutableEntry(e.getValue(), determineLabel.apply(e.getKey())))
////                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
////
////        return result;
////    }
//
//    public static String deriveLabelFromIri(String iriStr) {
//
//        String result;
//        for (;;) {
//            // Split XML returns invalid out-of-bound index for
//            // <http://dbpedia.org/resource/Ada_Apa_dengan_Cinta%3>
//            // This is what Node.getLocalName does
//            int idx = Util.splitNamespaceXML(iriStr);
//            result = idx == -1 || idx > iriStr.length() ? iriStr : iriStr.substring(idx);
//            if (result.isEmpty() && !iriStr.isEmpty() && idx != -1) {
//                iriStr = iriStr.substring(0, iriStr.length() - 1);
//                continue;
//            } else {
//                break;
//            }
//        } ;
//        return result;
//    }
//
//    public static String formatLiteralNode(Node node, PrefixMapping prefixMapping) {
//        String result;
//        if (node.isLiteral()) {
//            String dtIri = node.getLiteralDatatypeURI();
//            String dtPart = null;
//            if (dtIri != null) {
//                Entry<String, String> prefixToIri =
//                        PrefixUtils.findLongestPrefix(prefixMapping, dtIri);
//                dtPart = prefixToIri != null
//                        ? prefixToIri.getKey() + ":" + dtIri.substring(prefixToIri.getValue()
//                                .length())
//                        : "<" + dtIri + ">";
//            }
//
//            result = "\"" + node.getLiteralLexicalForm() + "\""
//                    + (dtPart == null ? "" : "^^" + dtPart);
//        } else {
//            result = Objects.toString(node);
//        }
//        System.out.println(result);
//        return result;
//    }
//
//
//    public static String getLabel(RDFNode node) {
//
//        Resource r = node.isResource() ? node.asResource() : null;
//        String result = r != null ? Optional.ofNullable(r.getProperty(labelProperty))
//                .map(Statement::getString)
//                .orElse(r.isURIResource() ? deriveLabelFromIri(r.getURI())
//                        : r.getId()
//                                .getLabelString())
//                : Objects.toString(Optional.ofNullable(node)
//                        .map(RDFNode::asNode)
//                        .orElse(null));
//
//       // System.out.println("RESULT: " + result + " for " + node);
//        return result;
//    }
//
//    public String toString(HLFacetConstraint<?> constraint) {
//        Expr expr = constraint.expr();
//        Set<Node> nodes = extractNodes(constraint);
//
//        Map<Node, String> nodeToLabel = getLabels(nodes, Function.identity());
//
//        Map<Node, String> bgpNodeLabels = indexPaths(constraint).entrySet()
//                .stream()
//                .collect(Collectors.toMap(Entry::getKey,
//                        e -> toString(e.getValue(), nodeToLabel::get)));
//
//        // Combine the maps to get the final label mapping
//        nodeToLabel.putAll(bgpNodeLabels);
//
//        String result = toString(expr, nodeToLabel::get);
//
//        return result;
//    }
//
//    public static String toString(SimplePath sp, Function<Node, String> nodeToStr) {
//        String result = sp.getSteps().stream()
//            .map(step -> toString(step, nodeToStr))
//            .collect(Collectors.joining("/"));
//
//        return result;
//    }
//
//    public static String toString(P_Path0 step, Function<Node, String> nodeToStr) {
//        Node node = step.getNode();
//        String result = (!step.isForward() ? "^" : "") + nodeToStr.apply(node);
//        return result;
//    }
//
//    public static Set<Node> extractNodes(HLFacetConstraint<?> constraint) {
//
//        Map<Node, FacetNode> map = constraint.mentionedFacetNodes();
//
//        Set<Node> nodes = new LinkedHashSet<>();
//
//        for (FacetNode fn : map.values()) {
//            BgpNode state = fn.as(FacetNodeResource.class)
//                    .state();
//            SimplePath simplePath = BgpNodeUtils.toSimplePath(state);
//            Set<Node> contrib = SimplePath.mentionedNodes(simplePath);
//
//            nodes.addAll(contrib);
//        }
//
//        // Add all iri and literal constants of the expr to the result
//        Expr expr = constraint.expr();
//        Set<Node> contrib = Streams
//                .stream(Traverser.<Expr>forTree(e -> e.isFunction() ? e.getFunction()
//                        .getArgs() : Collections.emptyList())
//                        .depthFirstPreOrder(expr))
//                .filter(Expr::isConstant)
//                .map(Expr::getConstant)
//                .map(NodeValue::asNode)
//                .filter(n -> n.isURI() || n.isLiteral())
//                .collect(Collectors.toSet());
//
//        nodes.addAll(contrib);
//
//        return nodes;
//    }
//
//    public static String toString(Expr expr, Function<? super Node, ? extends String> nodeToLabel) {
//        String result;
//        if (expr.isConstant()) {
//            Node node = expr.getConstant()
//                    .asNode();
//            result = nodeToLabel.apply(node);
//        } else if (expr.isVariable()) {
//            Node node = expr.asVar();
//            result = nodeToLabel.apply(node);
//        } else {
//            ExprFunction f = expr.getFunction();
//            String symbol = f.getFunctionSymbol()
//                    .getSymbol();
//            if (symbol == null || symbol.isEmpty()) {
//                symbol = f.getFunctionIRI();
//            }
//
//            List<String> argStrs = f.getArgs()
//                    .stream()
//                    .map(e -> toString(e, nodeToLabel))
//                    .collect(Collectors.toList());
//
//            result = argStrs.size() == 1 ? symbol + argStrs.iterator()
//                    .next()
//                    : argStrs.size() == 2 ? argStrs.get(0) + " " + symbol + " " + argStrs.get(1)
//                            : symbol + "(" + Joiner.on(",")
//                                    .join(argStrs) + ")";
//        }
//
//        return result;
//    }
//
//    public static Map<Node, SimplePath> indexPaths(HLFacetConstraint<?> constraint) {
//        Map<Node, SimplePath> result = constraint.mentionedFacetNodes()
//                .entrySet()
//                .stream()
//                .map(e -> {
//                    Node k = e.getKey();
//                    FacetNode fn = e.getValue();
//                    BgpNode state = fn.as(FacetNodeResource.class)
//                            .state();
//                    SimplePath simplePath = BgpNodeUtils.toSimplePath(state);
//                    return Maps.immutableEntry(k, simplePath);
//                })
//                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
//        return result;
//    }
//
//}