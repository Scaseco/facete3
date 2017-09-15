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
package org.hobbit.core.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.hobbit.core.Commands;
import org.hobbit.core.data.ResultPair;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ContainerStateObserverTest implements ContainerStateObserver {

    private static final String CORRECT_CONTAINER_NAME = "correctContainer";
    private static final String WRONG_CONTAINER_NAME = "wrongContainer";

    @Parameters
    public static Collection<Object[]> data() {
        List<Object[]> testConfigs = new ArrayList<Object[]>();

        testConfigs.add(new Object[] { new AbstractPlatformConnectorComponent() {
            @Override
            public void run() throws Exception {
            }
        } });

        testConfigs.add(new Object[] { new AbstractBenchmarkController() {
            @Override
            protected void executeBenchmark() throws Exception {
            }
        } });

        testConfigs.add(new Object[] { new AbstractDataGenerator() {
            @Override
            protected void generateData() throws Exception {
            }
        } });

        testConfigs.add(new Object[] { new AbstractEvaluationModule() {
            @Override
            protected Model summarizeEvaluation() throws Exception {
                return null;
            }

            @Override
            protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp,
                    long responseReceivedTimestamp) throws Exception {
            }
        } });

        testConfigs.add(new Object[] { new AbstractEvaluationStorage() {
            @Override
            public void receiveExpectedResponseData(String taskId, long timestamp, byte[] data) {
            }

            @Override
            public void receiveResponseData(String taskId, long timestamp, byte[] data) {
            }

            @Override
            protected Iterator<ResultPair> createIterator() {
                return null;
            }
        } });

        testConfigs.add(new Object[] { new AbstractSequencingTaskGenerator() {
            @Override
            protected void generateTask(byte[] data) throws Exception {
            }
        } });

        testConfigs.add(new Object[] { new AbstractSystemAdapter() {
            @Override
            public void receiveGeneratedTask(String taskId, byte[] data) {
            }

            @Override
            public void receiveGeneratedData(byte[] data) {
            }
        } });

        testConfigs.add(new Object[] { new AbstractTaskGenerator() {
            @Override
            protected void generateTask(byte[] data) throws Exception {
            }
        } });

        return testConfigs;
    }

    private AbstractPlatformConnectorComponent component;
    private int receivedExitCode = -1;
    private String receivedContainerName = null;

    public ContainerStateObserverTest(AbstractPlatformConnectorComponent component) {
        this.component = component;
    }

    @Test
    public void test() {
        // Add this object as observer for a container
        component.addContainerObserver(CORRECT_CONTAINER_NAME, this);
        // Some other container terminated
        emulateContainerTermination(component, WRONG_CONTAINER_NAME, 1);
        Assert.assertNull(receivedContainerName);
        // The observerd container terminated
        emulateContainerTermination(component, CORRECT_CONTAINER_NAME, 2);
        Assert.assertEquals(CORRECT_CONTAINER_NAME, receivedContainerName);
        Assert.assertEquals(2, receivedExitCode);
    }

    public void emulateContainerTermination(AbstractPlatformConnectorComponent component, String containerName,
            int exitCode) {
        component.receiveCommand(Commands.DOCKER_CONTAINER_TERMINATED, RabbitMQUtils.writeByteArrays(null,
                new byte[][] { RabbitMQUtils.writeString(containerName) }, new byte[] { (byte) exitCode }));
    }

    @Override
    public void containerStopped(String containerName, int exitCode) {
        receivedContainerName = containerName;
        receivedExitCode = exitCode;
    }

}
