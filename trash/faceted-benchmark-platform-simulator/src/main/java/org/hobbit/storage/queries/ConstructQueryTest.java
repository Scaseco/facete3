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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.hobbit.core.Constants;
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
public class ConstructQueryTest extends AbstractQueryTest {

    @Parameters
    public static Collection<Object[]> data() throws IOException {
        List<Object[]> testConfigs = new ArrayList<Object[]>();

        // Construct experiment graph
        testConfigs.add(new Object[] {
                "org/hobbit/storage/queries/exampleExperiment.ttl", SparqlQueries
                        .getExperimentGraphQuery("http://w3id.org/hobbit/experiments#LinkingExp10", FIRST_GRAPH_NAME),
                "org/hobbit/storage/queries/getExperimentResult.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleExperiment.ttl",
                SparqlQueries.getExperimentGraphQuery("http://w3id.org/hobbit/experiments#LinkingExp10", null),
                "org/hobbit/storage/queries/getExperimentResult.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleExperiment.ttl", SparqlQueries
                .getExperimentGraphQuery("http://w3id.org/hobbit/experiments#LinkingExp10", SECOND_GRAPH_NAME), null });

        // Construct shallow experiment graph
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleExperiment.ttl", SparqlQueries
                .getShallowExperimentGraphQuery("http://w3id.org/hobbit/experiments#LinkingExp10", FIRST_GRAPH_NAME),
                "org/hobbit/storage/queries/getShallowExperimentResult.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleExperiment.ttl",
                SparqlQueries.getShallowExperimentGraphQuery("http://w3id.org/hobbit/experiments#LinkingExp10", null),
                "org/hobbit/storage/queries/getShallowExperimentResult.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleExperiment.ttl", SparqlQueries
                .getShallowExperimentGraphQuery("http://w3id.org/hobbit/experiments#LinkingExp10", SECOND_GRAPH_NAME),
                null });

        // Construct experiment graph of challenge task
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleExperiment.ttl",
                SparqlQueries.getExperimentOfTaskQuery("http://w3id.org/hobbit/experiments#LinkingExp10",
                        "http://w3id.org/hobbit/challenges#OAEILinkingChallenge", FIRST_GRAPH_NAME),
                "org/hobbit/storage/queries/getExperimentOfTaskResult.ttl" });

        // Construct challenge graph
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getChallengeGraphQuery("http://example.org/MyChallenge", FIRST_GRAPH_NAME),
                "org/hobbit/storage/queries/getChallengeResult.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getChallengeGraphQuery("http://example.org/MyChallenge", null),
                "org/hobbit/storage/queries/getChallengeResult.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getChallengeGraphQuery("http://example.org/MyChallenge", SECOND_GRAPH_NAME), null });

        // Construct shallow challenge graph
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getShallowChallengeGraphQuery("http://example.org/MyChallenge", FIRST_GRAPH_NAME),
                "org/hobbit/storage/queries/getShallowChallengeResult.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getShallowChallengeGraphQuery("http://example.org/MyChallenge", null),
                "org/hobbit/storage/queries/getShallowChallengeResult.ttl" });
        testConfigs
                .add(new Object[] {
                        "org/hobbit/storage/queries/exampleChallengeConfig.ttl", SparqlQueries
                                .getShallowChallengeGraphQuery("http://example.org/MyChallenge", SECOND_GRAPH_NAME),
                        null });

        // Get tasks of challenge
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getChallengeTasksQuery("http://example.org/MyChallenge", FIRST_GRAPH_NAME),
                "org/hobbit/storage/queries/getChallengeTasksResult.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getChallengeTasksQuery("http://example.org/MyChallenge", null),
                "org/hobbit/storage/queries/getChallengeTasksResult.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getChallengeTasksQuery("http://example.org/MyChallenge", SECOND_GRAPH_NAME), null });

        // Construct experiment from challenge task
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getCreateExperimentFromTaskQuery(Constants.NEW_EXPERIMENT_URI,
                        "http://example.org/MyChallengeTask1", "http://example.org/SystemA", null),
                "org/hobbit/storage/queries/createExpFromTaskSystemA.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getCreateExperimentFromTaskQuery(Constants.NEW_EXPERIMENT_URI,
                        "http://example.org/MyChallengeTask1", "http://example.org/SystemC", null),
                null });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getCreateExperimentFromTaskQuery(Constants.NEW_EXPERIMENT_URI,
                        "http://example.org/MyChallengeTask2", "http://example.org/SystemC", null),
                "org/hobbit/storage/queries/createExpFromTaskSystemC.ttl" });

        // Construct experiment from challenge task
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/getExperimentForSystems.ttl",
                SparqlQueries.getExperimentGraphOfSystemsQuery(
                        Arrays.asList("http://w3id.org/system#limesV1", "http://w3id.org/system#limesV2"),
                        FIRST_GRAPH_NAME),
                "org/hobbit/storage/queries/getExperimentForSystemsResults.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/getExperimentForSystems.ttl",
                SparqlQueries.getExperimentGraphOfSystemsQuery(
                        Arrays.asList("http://w3id.org/system#limesV1", "http://w3id.org/system#limesV2"),
                        SECOND_GRAPH_NAME),
                null });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/getExperimentForSystems.ttl",
                SparqlQueries.getExperimentGraphOfSystemsQuery(
                        Arrays.asList("http://w3id.org/system#DoesNotExistV1", "http://w3id.org/system#DoesNotExistV2"),
                        FIRST_GRAPH_NAME),
                null });

        // Construct organizer model of challenge task
        testConfigs
                .add(new Object[] {
                        "org/hobbit/storage/queries/exampleChallengeConfig.ttl", SparqlQueries
                                .getChallengeTaskOrganizer("http://example.org/MyChallengeTask1", FIRST_GRAPH_NAME),
                        "org/hobbit/storage/queries/getChallengeOrganizerResult.ttl" });
        testConfigs
                .add(new Object[] {
                        "org/hobbit/storage/queries/exampleChallengeConfig.ttl", SparqlQueries
                                .getChallengeTaskOrganizer("http://example.org/MyChallengeTask1", SECOND_GRAPH_NAME),
                        null });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl", SparqlQueries
                .getChallengeTaskOrganizer("http://example.org/MyChallengeTask_DOES_NOT_EXIST", FIRST_GRAPH_NAME),
                null });

        // Construct challenge publish info graph
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/closedChallengeConfig.ttl",
                SparqlQueries.getChallengePublishInfoQuery(null, FIRST_GRAPH_NAME),
                "org/hobbit/storage/queries/getChallengePublishInfoResult.ttl" });
        testConfigs.add(new Object[] { "org/hobbit/storage/queries/exampleChallengeConfig.ttl",
                SparqlQueries.getChallengePublishInfoQuery(null, FIRST_GRAPH_NAME), null });

        return testConfigs;
    }

    public ConstructQueryTest(String storeContentResource, String query, String expectedResultResource) {
        super(storeContentResource, expectedResultResource, query);
    }

    @Override
    protected Model executeQueries(String[] queries, Dataset storeContent) {
        QueryExecution qe = QueryExecutionFactory.create(queries[0], storeContent);
        return qe.execConstruct();
    }

}
