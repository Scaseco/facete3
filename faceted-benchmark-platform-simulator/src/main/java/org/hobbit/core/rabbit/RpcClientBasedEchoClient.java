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

import java.util.Random;

import org.apache.commons.io.Charsets;
import org.junit.Ignore;

@Ignore
public class RpcClientBasedEchoClient implements Runnable {

    private RabbitRpcClient client;
    private Random random;
    private int numberOfMessages;

    public RpcClientBasedEchoClient(RabbitRpcClient client, int numberOfMessages, long seed) {
        super();
        this.client = client;
        this.numberOfMessages = numberOfMessages;
        random = new Random(seed);
    }

    @Override
    public void run() {
        try {
            String msg, rsp;
            for (int i = 0; i < numberOfMessages; ++i) {
                msg = Integer.toString(random.nextInt());
                rsp = new String(client.request(msg.getBytes(Charsets.UTF_8)), Charsets.UTF_8);
                if (!msg.equals(rsp)) {
                    System.err.println("Message \"" + msg + "\" and response \"" + rsp + "\" are not equal!");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
