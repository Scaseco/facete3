package org.hobbit.benchmarks.faceted_benchmark.main;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.hobbit.transfer.InputStreamManagerImpl;
import org.hobbit.transfer.StreamManager;

import com.esotericsoftware.kryo.Kryo;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RPCClient {

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";
    private String replyQueueName;

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();

        replyQueueName = channel.queueDeclare().getQueue();
    }

    public String call(String message) throws IOException, InterruptedException {
        String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();


        // Execute the command
        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        StreamManager sm = new InputStreamManagerImpl(null);

        sm.registerCallback((in) -> {
            System.out.println("Got a data stream");

            List<Object> items = StreamUtils.readObjectStream(in, new Kryo())
                    .collect(Collectors.toList());
                //String result = " foobar ";
            System.out.println(items);

        });




        int mode = 0;
        if(mode == 0) {
            channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrId)) {
                        //response.offer(new String(body, "UTF-8"));
                        sm.handleIncomingData(ByteBuffer.wrap(body));

                    }
                }
            });

        }
        else if(mode == 1) {
            final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);

            channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    if (properties.getCorrelationId().equals(corrId)) {
                        response.offer(new String(body, "UTF-8"));
                    }
                }
            });

            return response.take();
        } else if(mode == 3) {




            InputStream in = new InputStreamRabbitMQ(channel, replyQueueName, true, "myConsumerTag");
            List<Object> items = StreamUtils.readObjectStream(in, new Kryo())
                .collect(Collectors.toList());
            //String result = " foobar ";
            return "" + items;
        }

//        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
//
//        channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
//            @Override
//            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//                if (properties.getCorrelationId().equals(corrId)) {
//                    response.offer(new String(body, "UTF-8"));
//                }
//            }
//        });
//
//        return response.take();
        return "";
    }

    public void close() throws IOException {
        connection.close();
    }


    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        RPCClient fibonacciRpc = new RPCClient();

        System.out.println(" [x] Requesting fib(30)");
        String response = fibonacciRpc.call("30");
        System.out.println(" [.] Got '" + response + "'");

        fibonacciRpc.close();
        System.out.println("Client done");
    }
}
