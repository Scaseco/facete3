package org.hobbit.benchmarks.faceted_benchmark.main;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.hobbit.transfer.ChunkedProtocolWriterSimple;
import org.hobbit.transfer.OutputStreamChunkedTransfer;

import com.esotericsoftware.kryo.Kryo;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    public static void main(String[] argv) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        Connection connection = null;
        try {
            connection      = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();

                    //OutputStream out = new OutputStreamRabbitMQ(channel, "", properties.getReplyTo(), replyProps, false);

                    OutputStream out = OutputStreamChunkedTransfer.newInstanceForByteArrayDelegate(
                            new ChunkedProtocolWriterSimple(666),
                            data -> {
                                try { channel.basicPublish("", properties.getReplyTo(), replyProps, data);
                                } catch (Exception e) { throw new RuntimeException(e); }
                            },
                            null);

                    try {
                        String message = new String(body,"UTF-8");
                        int n = Integer.parseInt(message);

                        System.out.println(" [.] fib(" + message + ")");
                        //response += " foobar ";//fib(n);
                    }
                    catch (RuntimeException e){
                        System.out.println(" [.] " + e.toString());
                    }
                    finally {
//                        channel.basicPublish( "", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));

                        //List<Object> items = Arrays.asList("this", "is", "a", "test", 123, new Integer[]{1, 2, 3});
                        Stream<Object> stream = IntStream.range(0, 1000000).mapToObj(x -> x);
                        StreamUtils.writeObjectStream(out, stream, new Kryo(), false);
                        out.close();

                        channel.basicAck(envelope.getDeliveryTag(), false);

            // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized(this) {
                        this.notify();
                    }
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

            // Wait and be prepared to consume the message from RPC client.
        while (true) {
            synchronized(consumer) {
        try {
              consumer.wait();
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            }
         }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (connection != null)
        try {
                connection.close();
             } catch (IOException _ignore) {}
         }
    }
}