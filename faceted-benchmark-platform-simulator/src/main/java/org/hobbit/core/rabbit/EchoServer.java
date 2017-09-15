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

import org.junit.Ignore;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

@Ignore
public class EchoServer implements Runnable {

    private String rabbitHost;
    private boolean running = true;
    private String queueName;

    public EchoServer(String rabbitHost, String queueName) {
        super();
        this.rabbitHost = rabbitHost;
        this.queueName = queueName;
    }

    @Override
    public void run() {
        running = true;
        Connection connection = null;
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitHost);
            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(1);
            channel.queueDeclare(queueName, false, false, true, null);

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                        byte[] body) throws IOException {
                    BasicProperties replyProps = new BasicProperties.Builder()
                            .correlationId(properties.getCorrelationId()).deliveryMode(2).build();
                    channel.basicPublish("", properties.getReplyTo(), replyProps, body);
                }
            };
            channel.basicConsume(queueName, true, consumer);

            while (running) {
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

}
