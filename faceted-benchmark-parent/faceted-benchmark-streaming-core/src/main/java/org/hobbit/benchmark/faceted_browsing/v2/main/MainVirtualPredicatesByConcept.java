package org.hobbit.benchmark.faceted_browsing.v2.main;

import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.aksw.commons.graph.index.core.SubgraphIsomorphismIndex;
import org.aksw.jena_sparql_api.query_containment.index.ExpressionMapper;
import org.aksw.jena_sparql_api.query_containment.index.NodeMapperOp;
import org.aksw.jena_sparql_api.query_containment.index.NodeMapperOpContainment;
import org.aksw.jena_sparql_api.query_containment.index.OpContext;
import org.aksw.jena_sparql_api.query_containment.index.ResidualMatching;
import org.aksw.jena_sparql_api.query_containment.index.SparqlQueryContainmentIndex;
import org.aksw.jena_sparql_api.query_containment.index.SparqlQueryContainmentIndexImpl;
import org.aksw.jena_sparql_api.query_containment.index.SparqlTreeMapping;
import org.aksw.jena_sparql_api.stmt.SparqlElementParser;
import org.aksw.jena_sparql_api.stmt.SparqlElementParserImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.vocabulary.RDF;
import org.jgrapht.Graph;

import com.codepoetics.protonpack.functions.TriFunction;
import com.google.common.collect.BiMap;
import com.google.common.collect.Table;

public class MainVirtualPredicatesByConcept {
	public static void main(String[] args) {
		boolean validate = true;
    	TriFunction<OpContext, OpContext, Table<Op, Op, BiMap<Node, Node>>, NodeMapperOp> nodeMapperFactory = NodeMapperOpContainment::new; //(aContext, bContext) -> new NodeMapperOpContainment(aContext, bContext);
        SubgraphIsomorphismIndex<Entry<Node, Long>, Graph<Node, Triple>, Node> sii = ExpressionMapper.createIndex(validate);
        SparqlQueryContainmentIndex<Node, ResidualMatching> index = SparqlQueryContainmentIndexImpl.create(sii, nodeMapperFactory);
        
        
        SparqlElementParser p = new SparqlElementParserImpl();
        index.put(RDF.subject.asNode(), Algebra.compile(p.apply("?s a <http://example.org/Person>")));
        
        
        Op requestOp = Algebra.compile(p.apply("?x a <http://example.org/Person> ; a <http://foo.bar>"));
        List<Entry<Node, SparqlTreeMapping<ResidualMatching>>> list = index.match(requestOp).collect(Collectors.toList());
        
     
        System.out.println("Begin of matches");
        list.forEach(System.out::println);
        System.out.println("End of matches");
 	}
}
