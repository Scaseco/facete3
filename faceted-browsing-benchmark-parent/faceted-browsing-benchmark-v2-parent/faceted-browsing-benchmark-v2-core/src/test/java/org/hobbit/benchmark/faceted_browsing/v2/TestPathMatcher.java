package org.hobbit.benchmark.faceted_browsing.v2;

import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.aksw.jena_sparql_api.utils.DeltaWithFixedIterator;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.compose.Delta;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.SimplePathMatcher;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.SimplePathMatcher.NfaMatcher;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

public class TestPathMatcher {



	
	@Test
	public void testRemovalInDeltaModel() {
		JenaSystem.init();
		Model base = ModelFactory.createDefaultModel();
		Graph baseGraph = base.getGraph();
		base.add(RDF.type, RDF.type, OWL.Class);
		System.out.println(base);

		//Delta d = new Delta(base.getGraph());
		Delta d = new DeltaWithFixedIterator(base.getGraph());

		
		System.out.println("BaseGraph : " + baseGraph.hashCode());
		System.out.println("DeltaGraph: " + d.hashCode());
		
		Model m = ModelFactory.createModelForGraph(d);
		
		Resource s = RDF.type.inModel(m);
		ResourceUtils.setProperty(s, RDF.type, RDF.Property);
		
		
		System.out.println("Additions: " + d.getAdditions());
		System.out.println("Deletions: " + d.getDeletions());
		
		System.out.println(base);
	}
	
	@Test
	public void testStepMatcher() {
		JenaSystem.init();
		Path path = PathParser.parse("eg:a/eg:b/^eg:c", PrefixMapping.Extended);
		SimplePath sp = SimplePath.fromPropertyPath(path);
		NfaMatcher<P_Path0> matcher = SimplePathMatcher.createStepMatcher(path);

		for(P_Path0 step : sp.getSteps()) {
			matcher.advance(step);
			//System.out.println("path matcher - accepted / dead end: " + matcher.isAccepted() + "/" + matcher.isInDeadEnd());
		}
		
		Assert.assertTrue(matcher.isAccepted());
	}
	
	@Test
	public void testPathMatcher() {
		JenaSystem.init();
		Path path = PathParser.parse("eg:a/eg:b/^eg:c", PrefixMapping.Extended);

		//SimplePath sp = SimplePath.fromPropertyPath(PathParser.parse("eg:a/eg:b/eg:c", PrefixMapping.Extended));
		SimplePath sp = SimplePath.fromPropertyPath(path);

		Predicate<SimplePath> matcher = SimplePathMatcher.createPathMatcher(path);
		boolean matchResult = matcher.test(sp);
		Assert.assertTrue(matchResult);
	}
	
	
	@Test
	public void testPathMatcherRequireAtLastOneBackwardEdge() {
		JenaSystem.init();
		String anyFwd = "(eg:a|!eg:a)";
		String anyBwd = "^(eg:a|!eg:a)";
		
		String any = "(" + anyFwd + "|" + anyBwd + ")";
		String str = any + "*/" + anyBwd +"+/" + any + "*";
		
		System.out.println(str);
		Path path = PathParser.parse(str, PrefixMapping.Extended);
		Predicate<SimplePath> matcher = SimplePathMatcher.createPathMatcher(path);
//		System.out.println(path);

		Map<String, Boolean> map = ImmutableMap.<String, Boolean>builder()
				.put("^eg:x", true)
				.put("eg:a/^eg:x/eg:c", true)
				.put("eg:a/eg:b/eg:c", false)
				.put("eg:a/eg:b/eg:c/^eg:x", true)
				.build();
		
		for(Entry<String, Boolean> e : map.entrySet()) {
			String s = e.getKey();
			boolean expected = e.getValue();

			SimplePath sp = SimplePath.fromPropertyPath(PathParser.parse(s, PrefixMapping.Extended));
			boolean actual = matcher.test(sp);
			Assert.assertEquals(expected, actual);
		}
	}
}
