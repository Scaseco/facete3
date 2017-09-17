package org.hobbit.benchmarks.faceted_benchmark.main;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.hobbit.transfer.InputStreamChunkedTransfer;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;


public class InputStreamRabbitMQ
    extends InputStreamChunkedTransfer
{
    protected Channel channel;
    protected String queueName;
    protected boolean autoAck = false;
    protected String consumerTag;

    public InputStreamRabbitMQ(Channel channel, String queueName, boolean autoAck, String consumerTag) throws IOException {
        super();
        this.channel = channel;
        this.queueName = queueName;
        this.autoAck = autoAck;
        this.consumerTag = consumerTag;

        //this.queueName = channel.queueDeclare().getQueue();

        channel.basicConsume(queueName, autoAck, consumerTag,
             new DefaultConsumer(channel) {
                 @Override
                 public void handleDelivery(String consumerTag,
                                            Envelope envelope,
                                            AMQP.BasicProperties properties,
                                            byte[] body)
                     throws IOException
                 {
//                     String routingKey = envelope.getRoutingKey();
//                     String contentType = properties.getContentType();
                     long deliveryTag = envelope.getDeliveryTag();


                     ByteBuffer bodyBuffer = ByteBuffer.wrap(body);
                     long sequenceId = bodyBuffer.getLong();

                     System.out.println("Received message of size " + body.length + " and sequenceId " + sequenceId);

                     boolean lastBatchSeen = sequenceId < 0;
                     if(lastBatchSeen) {
                         setLastBatchSeen();
                     }

                     try {
                        appendDataToQueue(bodyBuffer);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }


                     // (process the message components here ...)
                     channel.basicAck(deliveryTag, false);
                 }
             });
    }

}
