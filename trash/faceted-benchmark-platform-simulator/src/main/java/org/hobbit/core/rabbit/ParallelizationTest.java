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
package org.hobbit.core.rabbit;

import java.io.IOException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import org.hobbit.core.TestConstants;
import org.junit.Test;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * This test starts a single {@link EchoServer}, creates a single
 * {@link RabbitRpcClient} instance and several {@link RpcClientBasedEchoClient}
 * instances that share the client instance. The clients send requests and wait
 * for their response. If one of the client gets stuck, this test will last
 * forever. Thus, it is counted as failing if it needs more time than
 * {@link #MAX_RUNTIME}, i.e., the main thread is stopped after that time.
 *
 * @author Michael R&ouml;der (roeder@informatik.uni-leipzig.de)
 *
 */
public class ParallelizationTest {

    public static final String REQUEST_QUEUE_NAME = "requestQueue";

    private static final int NUMBER_OF_CLIENTS = 5;
    private static final int NUMBER_OF_MSGS = 20000;
    private static final int MAX_RUNTIME = 120000;

    @Test
    public void test() throws InterruptedException, IOException, TimeoutException {

        EchoServer server = new EchoServer(TestConstants.RABBIT_HOST, REQUEST_QUEUE_NAME);

        Thread serverThread = new Thread(server);
        serverThread.start();
        System.out.println("Server started.");

        Random rand = new Random();
        Thread clientThreads[] = new Thread[NUMBER_OF_CLIENTS];
        RabbitRpcClient client = null;
        Connection connection = null;
        long time;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(TestConstants.RABBIT_HOST);
            connection = factory.newConnection();
            client = RabbitRpcClient.create(connection, REQUEST_QUEUE_NAME);
            for (int i = 0; i < clientThreads.length; ++i) {
                clientThreads[i] = new Thread(new RpcClientBasedEchoClient(client, NUMBER_OF_MSGS, rand.nextLong()));
            }

            Timer timer = new Timer();
            final Thread testThread = Thread.currentThread();
            timer.schedule(new TimerTask() {
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                    testThread.stop();
                }
            }, MAX_RUNTIME);

            time = System.currentTimeMillis();
            for (int i = 0; i < clientThreads.length; ++i) {
                clientThreads[i].start();
                System.out.print("Client #");
                System.out.print(i);
                System.out.println(" started.");
            }
            for (int i = 0; i < clientThreads.length; ++i) {
                clientThreads[i].join();
                System.out.print("Client #");
                System.out.print(i);
                System.out.println(" terminated.");
            }
            server.setRunning(false);
            serverThread.join();
            timer.cancel();

            time = System.currentTimeMillis() - time;
            System.out.println("Server terminated.");
            System.out.println("runtime : " + time + "ms");
        } finally {
            if (client != null) {
                client.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
