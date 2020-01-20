package org.aksw.facete.v3.experimental;

import org.aksw.commons.jena.graph.GraphVar;
import org.aksw.commons.jena.graph.GraphVarImpl;
import org.aksw.facete.v3.api.path.Resolver;
import org.aksw.jena_sparql_api.mapper.PartitionedQuery1;
import org.aksw.jena_sparql_api.utils.Vars;
import org.apache.jena.graph.GraphUtil;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.Template;

public class Resolvers {

	public static ResolverTemplate from(PartitionedQuery1 pq) {
		RDFNode node = toRdfModel(pq);
		ResolverTemplate result = new ResolverTemplate(null, pq, node, null, null);
		return result;
	}
	
	public static Resolver create() {
		PartitionedQuery1 pq = PartitionedQuery1.from(QueryFactory.create("CONSTRUCT WHERE {}"), Vars.s);
		Resolver result = Resolvers.from(pq);

		return result;
	}

	public static ResolverTemplate from(Var viewVar, Query view) {
		PartitionedQuery1 pq = PartitionedQuery1.from(view, viewVar);
		ResolverTemplate result = Resolvers.from(pq);

		return result;
	}
	
	public static RDFNode toRdfModel(PartitionedQuery1 pq) {
		Node rootNode = pq.getPartitionVar();
		
		Query query = pq.getQuery();
		Template template = query.getConstructTemplate();
		GraphVar graphVar = new GraphVarImpl(GraphFactory.createDefaultGraph());
		GraphUtil.add(graphVar, template.getTriples());
		Model model = ModelFactory.createModelForGraph(graphVar);
		
		Resource root = model.getRDFNode(rootNode).asResource();

		return root;
	}
}
