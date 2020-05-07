package org.aksw.facete3.app.vaadin;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.BinaryRelationImpl;
import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.lookup.LookupServiceUtils;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.PrefixUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.impl.Util;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.vocabulary.RDFS;

public class LabelService {

    private RDFConnection connection;
    private static PrefixMapping prefixes;
    private static LookupService<Node, String> labelLookupService;

    public LabelService(RDFConnection connection) {
        this.connection = connection;
        prefixes = new PrefixMappingImpl();
        labelLookupService = getLabelLookupService();
    }

    private LookupService<Node, String> getLabelLookupService() {
        BinaryRelation labelRelation = BinaryRelationImpl.create(RDFS.label);
        return LookupServiceUtils.createLookupService(connection, labelRelation)
                .partition(10)
                .cache()
                .mapValues(this::getLabelFromLookup);
    }

    private String getLabelFromLookup(Node node, List<Node> results) {
        String label = "";
        if (!results.isEmpty()) {
            Node labelNode = results.get(0);
            label = labelNode.toString();
        } else {
            String uri = node.getURI();
            label = deriveLabelFromIri(uri);
        }
        return label;
    }

    public <T extends RDFNode> void enrichWithLabels(Collection<T> rdfNodes,
            Function<? super T, ? extends Node> defineNodeForLabelFunction) {
        Multimap<Node, T> index = Multimaps.index(rdfNodes, defineNodeForLabelFunction::apply);
        Set<Node> s = index.keySet()
                .stream()
                .filter(Node::isURI)
                .collect(Collectors.toSet());
        Map<Node, String> map = labelLookupService.fetchMap(s);
        index.forEach((k, v) -> v.asResource()
                .addLiteral(RDFS.label,
                        map.getOrDefault(k,
                                NodeUtils.nullUriNode.equals(k) ? "(null)"
                                        : k.isURI() ? deriveLabelFromIri(k.getURI())
                                                : formatLiteralNode(k, prefixes))));
    }

    public static <T> Map<T, String> getLabels(Collection<T> rdfNodes,
            Function<? super T, ? extends Node> defineNodeForLabelFunction) {
        Multimap<Node, T> index = Multimaps.index(rdfNodes, defineNodeForLabelFunction::apply);
        Set<Node> labelNodes = index.keySet()
                .stream()
                .filter(Node::isURI)
                .collect(Collectors.toSet());
        Map<Node, String> map = labelLookupService.fetchMap(labelNodes);

        Function<Node, String> determineLabel = k -> map.getOrDefault(k,
                k.isURI() ? deriveLabelFromIri(k.getURI()) : formatLiteralNode(k, prefixes));

        Map<T, String> result = index.entries()
                .stream()
                .map(e -> Maps.immutableEntry(e.getValue(), determineLabel.apply(e.getKey())))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return result;
    }

    public static String deriveLabelFromIri(String iriStr) {

        String result;
        for (;;) {
            // Split XML returns invalid out-of-bound index for
            // <http://dbpedia.org/resource/Ada_Apa_dengan_Cinta%3>
            // This is what Node.getLocalName does
            int idx = Util.splitNamespaceXML(iriStr);
            result = idx == -1 || idx > iriStr.length() ? iriStr : iriStr.substring(idx);
            if (result.isEmpty() && !iriStr.isEmpty() && idx != -1) {
                iriStr = iriStr.substring(0, iriStr.length() - 1);
                continue;
            } else {
                break;
            }
        } ;
        return result;
    }

    public static String formatLiteralNode(Node node, PrefixMapping prefixMapping) {
        String result;
        if (node.isLiteral()) {
            String dtIri = node.getLiteralDatatypeURI();
            String dtPart = null;
            if (dtIri != null) {
                Entry<String, String> prefixToIri =
                        PrefixUtils.findLongestPrefix(prefixMapping, dtIri);
                dtPart = prefixToIri != null
                        ? prefixToIri.getKey() + ":" + dtIri.substring(prefixToIri.getValue()
                                .length())
                        : "<" + dtIri + ">";
            }

            result = "\"" + node.getLiteralLexicalForm() + "\""
                    + (dtPart == null ? "" : "^^" + dtPart);
        } else {
            result = Objects.toString(node);
        }

        return result;
    }

    public static String getLabel(RDFNode node) {
        Resource r = node.isResource() ? node.asResource() : null;

        String result = r != null ? Optional.ofNullable(r.getProperty(RDFS.label))
                .map(Statement::getString)
                .orElse(r.isURIResource() ? deriveLabelFromIri(r.getURI())
                        : r.getId()
                                .getLabelString())
                : Objects.toString(Optional.ofNullable(node)
                        .map(RDFNode::asNode)
                        .orElse(null));

        // System.out.println("RESULT: " + result + " for " + node);
        return result;
    }

}
