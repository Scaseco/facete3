package org.hobbit.benchmarks.faceted_benchmark.main;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.rabbitmq.client.Channel;


public class CommandBrokerRabbitMq
    implements CommandBroker
{
    protected Map<String, Function<List<?>, Stream<?>>> commandNameToImpl;

    protected Channel channel;

    
    @Override
    public <T> void registerStreamingCommand(String name, Function<List<?>, Stream<T>> callable) {
        Consumer consumer = new DefaultConsumer(cmdChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                    byte[] body) throws IOException {
                try {
                    handleCmd(body, properties.getReplyTo());
                } catch (Exception e) {
                    LOGGER.error("Exception while trying to handle incoming command.", e);
                }
            }
        };
        cmdChannel.basicConsume(queueName, true, consumer);

    }

    @Override
    public <T> Stream<T> executeStreamingCommand(String name, Object... args) {
        // TODO Auto-generated method stub
        return null;
    }

}
