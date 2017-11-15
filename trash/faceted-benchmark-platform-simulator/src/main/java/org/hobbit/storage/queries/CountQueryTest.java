/**
 * This file is part of core.
 *
 * core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with core.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.hobbit.storage.queries;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This test class tests SPARQL CONSTRUCT queries.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
@RunWith(Parameterized.class)
public class CountQueryTest {

    @Parameters
    public static Collection<Object[]> data() throws IOException {
        List<Object[]> testConfigs = new ArrayList<Object[]>();

        testConfigs.add(new Object[] { "org/hobbit/storage/queries/selectChallengeTaskExpCount.ttl",
                SparqlQueries.countExperimentsOfTaskQuery("http://example.org/task1", AbstractQueryTest.FIRST_GRAPH_NAME),
                "count", 3 });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/selectChallengeTaskExpCount.ttl",
                SparqlQueries.countExperimentsOfTaskQuery("http://example.org/task1", null), "count", 3 });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/selectChallengeTaskExpCount.ttl",
                SparqlQueries.countExperimentsOfTaskQuery("http://example.org/task1", AbstractQueryTest.SECOND_GRAPH_NAME),
                "count", 0 });

        testConfigs.add(new Object[] { "org/hobbit/storage/queries/selectChallengeTaskExpCount.ttl",
                SparqlQueries.countExperimentsOfTaskQuery("http://example.org/task2", AbstractQueryTest.FIRST_GRAPH_NAME),
                "count", 2 });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/selectChallengeTaskExpCount.ttl",
                SparqlQueries.countExperimentsOfTaskQuery("http://example.org/task2", null), "count", 2 });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/selectChallengeTaskExpCount.ttl",
                SparqlQueries.countExperimentsOfTaskQuery("http://example.org/task2", AbstractQueryTest.SECOND_GRAPH_NAME),
                "count", 0 });

        testConfigs.add(new Object[] { "org/hobbit/storage/queries/selectChallengeTaskExpCount.ttl",
                SparqlQueries.countExperimentsOfTaskQuery("http://example.org/task3", AbstractQueryTest.FIRST_GRAPH_NAME),
                "count", 1 });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/selectChallengeTaskExpCount.ttl",
                SparqlQueries.countExperimentsOfTaskQuery("http://example.org/task3", null), "count", 1 });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/selectChallengeTaskExpCount.ttl",
                SparqlQueries.countExperimentsOfTaskQuery("http://example.org/task3", AbstractQueryTest.SECOND_GRAPH_NAME),
                "count", 0 });

        return testConfigs;
    }

    private String storeContentResource;
    private String query;
    private String resultVariable;
    private Object expectedResult;

    public CountQueryTest(String storeContentResource, String query, String resultVariable, Object expectedResult) {
        this.storeContentResource = storeContentResource;
        this.query = query;
        this.resultVariable = resultVariable;
        this.expectedResult = expectedResult;
    }

    @Test
    public void test() {
        // Make sure the query has been loaded correctly
        Assert.assertNotNull(query);
        // load the models
        Dataset storeContent = DatasetFactory.createTxnMem();
        // If the named graph is not empty, load it
        if (storeContentResource != null) {
            storeContent.addNamedModel(AbstractQueryTest.FIRST_GRAPH_NAME,
                    AbstractQueryTest.loadModel(storeContentResource));
        } else {
            storeContent.addNamedModel(AbstractQueryTest.FIRST_GRAPH_NAME, ModelFactory.createDefaultModel());
        }
        storeContent.addNamedModel(AbstractQueryTest.SECOND_GRAPH_NAME, ModelFactory.createDefaultModel());

        // execute query
        QueryExecution qe = QueryExecutionFactory.create(query, storeContent);
        ResultSet results = qe.execSelect();

        Assert.assertTrue(results.hasNext());
        QuerySolution solution = results.next();
        Assert.assertTrue(solution.contains(resultVariable));
        Assert.assertEquals(expectedResult, solution.getLiteral(resultVariable).getInt());
        Assert.assertFalse(results.hasNext());
    }

}
