package org.aksw.vaadin.datashape.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aksw.commons.collection.observable.ObservableCollection;
import org.aksw.commons.path.core.Path;
import org.aksw.jena_sparql_api.collection.GraphChange;
import org.aksw.jena_sparql_api.collection.RdfField;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.lookup.MapService;
import org.aksw.jena_sparql_api.path.core.PathOpsNode;
import org.aksw.jena_sparql_api.path.datatype.RDFDatatypePath;
import org.aksw.jena_sparql_api.schema.ShapedNode;
import org.aksw.jena_sparql_api.schema.ShapedProperty;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.path.P_Path0;

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
    protected boolean showEmptyProperties = true;


    protected GraphChange graphEditorModel;


//    public HierarchicalDataProviderForShacl(MapService<Concept, Node, ShapedNode> root) {
//        this(root, null);
//    }

    public HierarchicalDataProviderForShacl(MapService<Concept, Node, ShapedNode> root, GraphChange graphEditorModel) {
        super();
        this.root = root;
        this.graphEditorModel = graphEditorModel;
    }


    @Override
    public int getChildCount(HierarchicalQuery<Path<Node>, String> query) {
        int result = Ints.saturatedCast(fetchChildrenFromBackEnd(query).count());
        return result;
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

        // Remember the last node and path
        Node s = null;
        org.apache.jena.sparql.path.Path path = null;

        int n = basePath == null ? 0 : basePath.getNameCount();
        for (int i = 0; i < n; i +=2) {
            s = basePath.getName(i).toSegment();

            ShapedNode sn = current.createPaginator(ConceptUtils.createConcept(s)).fetchMap().get(s);

            if (sn != null) {
                Map<org.apache.jena.sparql.path.Path, ShapedProperty> map = sn.getShapedProperties();
                if (i + 1 < n) {
                    Node p = basePath.getName(i + 1).toSegment();

                    if (p.isLiteral() && p.getLiteralValue() instanceof org.apache.jena.sparql.path.Path) {
                        path = (org.apache.jena.sparql.path.Path)p.getLiteralValue();

                        ShapedProperty sp = map.get(path);

                        current = sp.getValues();

                        result = current.createPaginator(null).fetchMap()
                                .values().stream().map(ShapedNode::getSourceNode).map(basePath::resolve);


                    }

                } else {

                    result = map.entrySet().stream()
                                .filter(e -> showEmptyProperties || !e.getValue().isEmpty()) // Filter out empty properties
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


        Collection<Path<Node>> addedPaths = Collections.emptyList();
        boolean isPropertyPath = n % 2 == 0;
        if (isPropertyPath && path != null) {

            P_Path0 p0 = (P_Path0)path;

            RdfField rdfField = graphEditorModel.createSetField(s, p0.getNode(), p0.isForward());

            // ObservableCollection<Node> existingValues = rdfField.getBaseAsSet();
            ObservableCollection<Node> addedValues = rdfField.getAddedAsSet();

            addedPaths = addedValues.stream().map(basePath::resolve).collect(Collectors.toList());


            System.out.println("Added paths: " + addedPaths);
            int m = addedValues.size();


        }


        if (result == null) {
            result = root.fetchData(null).values().stream()
                    .map(sn -> PathOpsNode.newAbsolutePath().resolve(sn.getSourceNode()));
            // result = Stream.empty();
        }

        List<Path<Node>> tmp = result.collect(Collectors.toList());
        tmp = Stream.concat(addedPaths.stream(), tmp.stream()).collect(Collectors.toList());

        // System.out.println("Data provider for path " + basePath + ": " + tmp);
        return tmp.stream();
    }


}
