package org.aksw.vaadin.datashape.provider;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.aksw.jena_sparql_api.path.core.PathOpsNode;
import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePath;
import org.aksw.jena_sparql_api.schema.ShapedNode;
import org.aksw.jena_sparql_api.schema.ShapedProperty;
import org.aksw.jena_sparql_api.util.sparql.syntax.path.PathUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.vaadin.flow.data.provider.hierarchy.AbstractBackEndHierarchicalDataProvider;
import com.vaadin.flow.data.provider.hierarchy.HierarchicalQuery;

public class HierarchicalDataProviderForShacl
    extends AbstractBackEndHierarchicalDataProvider<Path<Node>, String>
{
    private static final long serialVersionUID = 1L;

    // protected ShapedNode rootNode;
    protected MapService<Concept, Node, ShapedNode> root;
    // protected Set<NodeSchema> rootSchemas;
    //protected SparqlQueryConnection conn;
    protected boolean showEmptyProperties = false;


    @Override
    public int getChildCount(HierarchicalQuery<Path<Node>, String> query) {
        int result = Ints.saturatedCast(fetchChildrenFromBackEnd(query).count());
        return result;
    }

    public HierarchicalDataProviderForShacl(MapService<Concept, Node, ShapedNode> root) {
        super();
        this.root = root;
    }

    @Override
    public boolean hasChildren(Path<Node> item) {
        HierarchicalQuery<Path<Node>, String> hq = new HierarchicalQuery<>(0, 1, Collections.emptyList(), null, null, item);

        boolean result = fetchChildrenFromBackEnd(hq).findAny().isPresent();
        return result;
    }


    @Override
    protected Stream<Path<Node>> fetchChildrenFromBackEnd(HierarchicalQuery<Path<Node>, String> query) {

        Range<Long> range = Range.closedOpen((long)query.getOffset(), (long)(query.getOffset() + query.getLimit()));

        Stream<Path<Node>> result = null;

        Path<Node> basePath = query.getParent();
        MapService<Concept, Node, ShapedNode> current = root;


        // Resolve the node shape for the final resource in the path

        int n = basePath == null ? 0 : basePath.getNameCount();
        for (int i = 0; i < n; i+=2) {
            Node s = basePath.getName(i).toSegment();

            ShapedNode sn = current.createPaginator(ConceptUtils.createConcept(s)).fetchMap().get(s);

            if (sn != null) {
                Map<org.apache.jena.sparql.path.Path, ShapedProperty> map = sn.getShapedProperties();
                if (i + 1 < n) {
                    Node p = basePath.getName(i + 1).toSegment();

                    if (p.isLiteral() && p.getLiteralValue() instanceof org.apache.jena.sparql.path.Path) {
                        org.apache.jena.sparql.path.Path path = (org.apache.jena.sparql.path.Path)p.getLiteralValue();

                        ShapedProperty sp = map.get(path);

                        current = sp.getValues();

                        result = current.createPaginator(null).fetchMap()
                                .values().stream().map(ShapedNode::getSourceNode).map(basePath::resolve);


                    }

                } else {

                    result = map.entrySet().stream()
                                .filter(e -> !e.getValue().isEmpty()) // Filter out empty properties
                                .map(Entry::getKey)
                                .map(p -> NodeFactory.createLiteralByValue(p, RDFDatatypePath.INSTANCE))
                                .map(basePath::resolve);

                }
            } else {
                current = null;
                result = Stream.empty();
                break;
            }
        }

        if (result == null) {
            result = root.fetchData(null).values().stream()
                    .map(sn -> PathOpsNode.newAbsolutePath().resolve(sn.getSourceNode()));
            // result = Stream.empty();
        }

        List<Path<Node>> tmp = result.collect(Collectors.toList());

        // System.out.println("Data provider for path " + basePath + ": " + tmp);
        return tmp.stream();
    }


}
