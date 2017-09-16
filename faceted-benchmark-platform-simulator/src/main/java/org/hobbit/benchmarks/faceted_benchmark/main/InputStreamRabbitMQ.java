package org.hobbit.benchmarks.faceted_benchmark.main;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;


public class InputStreamRabbitMQ
    extends InputStream
{
    protected Channel channel;
    protected String queueName;
    protected boolean autoAck = false;
    protected String consumerTag;

    protected boolean lastBatchSeen = false;
    protected BlockingQueue<ByteBuffer> clientQueue = new LinkedBlockingQueue<>();
    protected ByteBuffer currentBuffer;


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

                     lastBatchSeen = sequenceId < 0;

                     try {
                        clientQueue.put(bodyBuffer);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }


                     // (process the message components here ...)
                     //channel.basicAck(deliveryTag, false);
                 }
             });
    }




    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int remaining = len;
        int result = 0;

        while(remaining > 0) {
            int available = currentBuffer == null ? 0 : currentBuffer.remaining();
            if(available == 0) {
                // If we are at the last batch and have not read anything yet, we have reached the end
                if(lastBatchSeen && clientQueue.isEmpty()) {
                    if(result == 0) {
                        result = -1;
                    }
                    break;
                } else {
                    try {
                        currentBuffer = clientQueue.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    continue;
                }
            }

            int toRead = Math.min(remaining, available);
            result += toRead;
            currentBuffer.get(b, off, toRead);
            off += toRead;
            remaining -= toRead;
        }

        return result;
    }

    @Override
    public int read() throws IOException {
        byte tmp[] = {0};
        int code = read(tmp, 0, 1);
        int result = code > 0 ? tmp[0] : -1;
        return result;
    }

}
