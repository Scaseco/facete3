package org.hobbit.benchmark.faceted_browsing.v2;

import java.util.function.Predicate;

import org.aksw.jena_sparql_api.util.sparql.syntax.path.SimplePath;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.sparql.path.Path;
import org.apache.jena.sparql.path.PathParser;
import org.apache.jena.sys.JenaSystem;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.SimplePathMatcher;
import org.hobbit.benchmark.faceted_browsing.v2.task_generator.SimplePathMatcher.NfaMatcher;
import org.junit.Assert;
import org.junit.Test;

public class TestPathMatcher {

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
}
