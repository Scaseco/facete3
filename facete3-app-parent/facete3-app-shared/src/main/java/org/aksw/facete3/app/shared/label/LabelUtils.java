package org.aksw.facete3.app.shared.label;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.aksw.jena_sparql_api.lookup.LookupService;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.utils.NodeUtils;
import org.aksw.jena_sparql_api.utils.PrefixUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.util.SplitIRI;
import org.apache.jena.vocabulary.RDFS;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import io.reactivex.rxjava3.core.Flowable;

public class LabelUtils {

    /**
     * A method similar to {@link NodeFmtLib#displayStr(Node)} however it
     * accepts a {@link PrefixMapping} instead of a {@link PrefixMap}.
     *
     * @param node
     * @param prefixMapping
     * @return
     */
    public static String str(Node node, PrefixMapping prefixMapping) {
        PrefixMap pm = prefixMapping == null
                ? null
                : new PrefixMapAdapter(prefixMapping);

        String result = node == null
                ? "(null)"
                : NodeFmtLib.str(node, pm);

        return result;

    }

    /**
     * An wrapper for {@link #getLabels(Collection, Function, LookupService, PrefixMapping)} that attaches
     * the obtained labels to resources
     *
     * @param <T>
     * @param cs
     * @param nodeFunction
     * @param labelService
     * @param prefixes
     */
    public static <T extends RDFNode> void enrichWithLabels(
            Collection<T> cs,
            LookupService<T, String> labelService) {
        Map<T, String> labelMap = labelService.fetchMap(cs);

        for (Entry<T, String> e : labelMap.entrySet()) {
            RDFNode rdfNode = e.getKey();
            String label = e.getValue();
            if (rdfNode.isResource()) {
                Resource k = rdfNode.asResource();
                ResourceUtils.setLiteralProperty(k, RDFS.label, label);
            }
        }
    }


    public static String deriveLabelFromIri(String iriStr, PrefixMapping pm) {
        String result = null;
        if (pm != null) {
            Entry<String, String> entry = PrefixUtils.findLongestPrefix(pm, iriStr);
            if (entry != null) {
                String localName = iriStr.substring(entry.getValue().length());
                result = entry.getKey() + ":" + localName;
            }
        }

        if (result == null) {
            result = deriveLabelFromIri(iriStr);
        }

        return result;
    }

    public static String deriveLabelFromIri(String iriStr) {
        String result;

        // There are fragments such as #this #self #me #service which are not useful as a label
        // If there are less-than-equal n characters after a hash use a slash as the splitpoint
        int n = 6;
        int lastIdx = iriStr.length() - 1;
        int slashIdx = iriStr.lastIndexOf('/');
        int hashIdx = iriStr.lastIndexOf('#');
        if ((lastIdx - hashIdx) <= n && slashIdx < hashIdx && slashIdx != -1) {
            result = iriStr.substring(slashIdx + 1);
        } else {
            result = SplitIRI.localname(iriStr);
        }

        return result;
    }

//    public static String deriveLabelFromIri(String iriStr) {
//
//        String result;
//        for(;;) {
//            // Split XML returns invalid out-of-bound index for <http://dbpedia.org/resource/Ada_Apa_dengan_Cinta%3>
//            // This is what Node.getLocalName does
//            int idx = Util.splitNamespaceXML(iriStr);
//            result = idx == -1 || idx > iriStr.length() ? iriStr : iriStr.substring(idx);
//            if(result.isEmpty() && !iriStr.isEmpty() && idx != -1) {
//                iriStr = iriStr.substring(0, iriStr.length() - 1);
//                continue;
//            } else {
//                break;
//            }
//        };
//        return result;
//    }


    /**
     * An alternative approach where the label is stored in a simple wrapper object
     *
     * @param <T>
     * @param cs
     * @param itemToLabel
     * @return
     */
    public static <T> Collection<Labeled<T>> wrapWithLabel(Collection<T> cs, Function<? super T, ? extends String> itemToLabel) {
        Collection<Labeled<T>> result = cs.stream()
                .map(item -> new LabeledImpl<T>(item, itemToLabel.apply(item)))
                .collect(Collectors.toList());

        return result;
    }



    /**
     * Note: Returning the longest prefix is concern of the prefix map implementation.
     * So this method was the wrong place to add this behavior
     *
     * Formats a node to a (parseable) string w.r.t. a given prefix mapping.
     * This method is similar to {@link NodeFmtLib#str(Node)} however it always
     * picks the longest prefix for the datatype IRI.
     *
     * @param node
     * @param prefixMapping
     * @return
     */
//    public static String formatLiteralNode(Node node, PrefixMapping prefixMapping) {
//        String result;
//        if(node.isLiteral()) {
//            String dtIri = node.getLiteralDatatypeURI();
//            String dtPart = null;
//            if(dtIri != null) {
//                Entry<String, String> prefixToIri = prefixMapping == null
//                    ? null
//                    : PrefixUtils.findLongestPrefix(prefixMapping, dtIri);
//
//                dtPart = prefixToIri != null
//                    ? prefixToIri.getKey() + ":" + dtIri.substring(prefixToIri.getValue().length())
//                    : "<" + dtIri + ">";
//            }
//
//            result = "\"" + node.getLiteralLexicalForm() + "\""
//                    + (dtPart == null ? "" : "^^" + dtPart);
//        } else {
//            result = Objects.toString(node);
//        }
//
//        return result;
//    }


    public static <T> LookupService<T, String> createLookupServiceForLabels(
            Function<? super T, ? extends Node> nodeFunction,
            LookupService<Node, String> labelService,
            PrefixMapping iriPrefixes,
            PrefixMapping literalPrefixes
    ) {
        return cs -> Flowable.fromIterable(getLabels(cs, nodeFunction, labelService, iriPrefixes, literalPrefixes).entrySet());
    }


    public static String deriveLabelFromNode(Node node, PrefixMapping iriPrefixes, PrefixMapping literalPrefixes) {
        String result = node == null || NodeUtils.nullUriNode.equals(node)
            ? "(null)"
            : node.isURI()
                ? deriveLabelFromIri(node.getURI(), iriPrefixes)
                : NodeFmtLib.str(node, new PrefixMapAdapter(literalPrefixes));

        return result;
    }

    public static <T> Map<T, String> getLabels(
            Iterable<T> cs, Function<? super T, ? extends Node> nodeFunction,
            LookupService<Node, String> labelService,
            PrefixMapping iriPrefixes,
            PrefixMapping literalPrefixes) {
//        Multimap<Node, T> index = Multimaps.index(cs, nodeFunction::apply);
        Multimap<Node, T> index = Multimaps.index(cs, item ->
            Optional.<Node>ofNullable(nodeFunction.apply(item)).orElse(NodeUtils.nullUriNode));

        Set<Node> s = index.keySet().stream().filter(Node::isURI).collect(Collectors.toSet());
        Map<Node, String> map = labelService.fetchMap(s);

        Function<Node, String> determineLabel = k -> {
            String r = map.get(k);
            if (r == null) {
                r = deriveLabelFromNode(k, iriPrefixes, literalPrefixes);
            }
            return r;
        };

        Map<T, String> result =
            index.entries().stream().map(
            e -> Maps.immutableEntry(e.getValue(), determineLabel.apply(e.getKey())))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        return result;
    }

}
