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

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This test class tests SPARQL UPDATE queries.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
@RunWith(Parameterized.class)
public class UpdateQueryTest extends AbstractQueryTest {

    @SuppressWarnings("deprecation")
    @Parameters
    public static Collection<Object[]> data() throws IOException {
        List<Object[]> testConfigs = new ArrayList<Object[]>();

        // Close challenge update
        /*
         * A normal challenge is closed after the update is executed.
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                "org/hobbit/storage/queries/closedChallengeConfig.ttl", FIRST_GRAPH_NAME, new String[] {
                        SparqlQueries.getCloseChallengeQuery("http://example.org/MyChallenge", FIRST_GRAPH_NAME) } });
        /*
         * An already closed challenge stays closed.
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/closedChallengeConfig.ttl",
                "org/hobbit/storage/queries/closedChallengeConfig.ttl", FIRST_GRAPH_NAME, new String[] {
                        SparqlQueries.getCloseChallengeQuery("http://example.org/MyChallenge", FIRST_GRAPH_NAME) } });
        /*
         * An empty graph is not changed.
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl", null, SECOND_GRAPH_NAME,
                new String[] {
                        SparqlQueries.getCloseChallengeQuery("http://example.org/MyChallenge", SECOND_GRAPH_NAME) } });

        // Check the model diff based SPARQL UPDATE query creation
        Model original, updated;
        original = loadModel("org/hobbit/storage/queries/exampleChallengeConfig.ttl");
        updated = loadModel("org/hobbit/storage/queries/changedChallengeConfig.ttl");
        /*
         * The original model is changed to the updated model as expected.
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                "org/hobbit/storage/queries/changedChallengeConfig.ttl", FIRST_GRAPH_NAME,
                new String[] { SparqlQueries.getUpdateQueryFromDiff(original, updated, FIRST_GRAPH_NAME) } });
        /*
         * A query that should focus on the second graph does not change the
         * first graph
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                "org/hobbit/storage/queries/exampleChallengeConfig.ttl", FIRST_GRAPH_NAME,
                new String[] { SparqlQueries.getUpdateQueryFromDiff(original, updated, SECOND_GRAPH_NAME) } });
        /*
         * A query that should DELETE and INSERT something does not change an
         * empty graph.
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl", null, SECOND_GRAPH_NAME,
                new String[] { SparqlQueries.getUpdateQueryFromDiff(original, updated, SECOND_GRAPH_NAME) } });
        /*
         * The original model is changed to the updated model as expected with
         * the possibility to create multiple queries.
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                "org/hobbit/storage/queries/changedChallengeConfig.ttl", FIRST_GRAPH_NAME,
                SparqlQueries.getUpdateQueriesFromDiff(original, updated, FIRST_GRAPH_NAME) });
        /*
         * The original model is changed to the updated model as expected with
         * one triple per query
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                "org/hobbit/storage/queries/changedChallengeConfig.ttl", FIRST_GRAPH_NAME,
                SparqlQueries.getUpdateQueriesFromDiff(original, updated, FIRST_GRAPH_NAME, 1) });
        /*
         * The original model is changed to the updated model as expected with
         * up to two triples per query
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                "org/hobbit/storage/queries/changedChallengeConfig.ttl", FIRST_GRAPH_NAME,
                SparqlQueries.getUpdateQueriesFromDiff(original, updated, FIRST_GRAPH_NAME, 2) });
        /*
         * A query that should focus on the second graph does not change the
         * first graph
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                "org/hobbit/storage/queries/exampleChallengeConfig.ttl", FIRST_GRAPH_NAME,
                SparqlQueries.getUpdateQueriesFromDiff(original, updated, SECOND_GRAPH_NAME) });
        /*
         * A query that should DELETE and INSERT something does not change an
         * empty graph.
         */
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl", null, SECOND_GRAPH_NAME,
                SparqlQueries.getUpdateQueriesFromDiff(original, updated, SECOND_GRAPH_NAME) });
        /*
         * The difference between an open and a closed challenge can be
         * expressed with this method as well.
         */
        updated = loadModel("org/hobbit/storage/queries/closedChallengeConfig.ttl");
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                "org/hobbit/storage/queries/closedChallengeConfig.ttl", FIRST_GRAPH_NAME,
                new String[] { SparqlQueries.getUpdateQueryFromDiff(original, updated, FIRST_GRAPH_NAME) } });

        // Delete experiment
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleExperiment.ttl",
                "org/hobbit/storage/queries/deletedExperiment.ttl", FIRST_GRAPH_NAME,
                new String[] { SparqlQueries.deleteExperimentGraphQuery(
                        "http://w3id.org/hobbit/experiments#LinkingExp10", FIRST_GRAPH_NAME) } });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleExperiment.ttl",
                "org/hobbit/storage/queries/exampleExperiment.ttl", FIRST_GRAPH_NAME,
                new String[] { SparqlQueries.deleteExperimentGraphQuery(
                        "http://w3id.org/hobbit/experiments#LinkingExp10", SECOND_GRAPH_NAME) } });

        // Delete challenge
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/deleteChallengeExample.ttl",
                "org/hobbit/storage/queries/cleanUpChallengeExample1.ttl", FIRST_GRAPH_NAME,
                new String[] { SparqlQueries.deleteChallengeGraphQuery("http://example.org/MyChallenge",
                        FIRST_GRAPH_NAME) } });

        // Check the clean up challenge config query
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/cleanUpChallengeExample1.ttl",
                "org/hobbit/storage/queries/cleanUpChallengeExample1Result.ttl", FIRST_GRAPH_NAME,
                new String[] { SparqlQueries.cleanUpChallengeGraphQuery(FIRST_GRAPH_NAME) } });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/cleanUpChallengeExample1.ttl",
                "org/hobbit/storage/queries/cleanUpChallengeExample1Result.ttl", FIRST_GRAPH_NAME,
                SparqlQueries.cleanUpChallengeGraphQueries(FIRST_GRAPH_NAME) });
        // an additional test for the cleaning of parameters
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/cleanUpChallenge_Parameter.ttl", null,
                FIRST_GRAPH_NAME, SparqlQueries.cleanUpChallengeGraphQueries(FIRST_GRAPH_NAME) });
        // Check the clean up challenge config query with an already clean
        // config graph
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/cleanUpChallengeExample2.ttl",
                "org/hobbit/storage/queries/cleanUpChallengeExample2.ttl", FIRST_GRAPH_NAME,
                new String[] { SparqlQueries.cleanUpChallengeGraphQuery(FIRST_GRAPH_NAME) } });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/cleanUpChallengeExample2.ttl",
                "org/hobbit/storage/queries/cleanUpChallengeExample2.ttl", FIRST_GRAPH_NAME,
                SparqlQueries.cleanUpChallengeGraphQueries(FIRST_GRAPH_NAME) });
        return testConfigs;
    }

    private String graphUri;

    public UpdateQueryTest(String storeContentResource, String expectedResultResource, String graphUri,
            String[] queries) {
        super(storeContentResource, expectedResultResource, queries);
        this.graphUri = graphUri;
    }

    @Override
    protected Model executeQueries(String[] queries, Dataset storeContent) {
        DatasetGraph dg = storeContent.asDatasetGraph();
        for (int i = 0; i < queries.length; ++i) {
            UpdateRequest update = UpdateFactory.create(queries[i]);
            UpdateProcessor up = UpdateExecutionFactory.create(update, dg);
            up.execute();
            dg = up.getDatasetGraph();
        }
        return ModelFactory.createModelForGraph(dg.getGraph(NodeFactory.createURI(graphUri)));
    }

}
