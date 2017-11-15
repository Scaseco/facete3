package org.hobbit.config.platform;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

import org.hobbit.transfer.Publisher;
import org.hobbit.transfer.PublishingWritableByteChannel;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class PublisherRabbitMQ
	extends DefaultConsumer
	implements Publisher<ByteBuffer>
{
	protected PublishingWritableByteChannel delegate;
	
	public PublisherRabbitMQ(Channel channel) {
		super(channel);
	}

	@Override
	public Runnable subscribe(Consumer<? super ByteBuffer> subscriber) {
		Runnable result = delegate.subscribe(subscriber);
		return result;
	}

	@Override
	public void unsubscribe(Consumer<? super ByteBuffer> subscribe) {
		delegate.unsubscribe(subscribe);
	}

	
	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
    	ByteBuffer buffer = ByteBuffer.wrap(body);
    	delegate.write(buffer);
	}


	public static PublisherRabbitMQ create(Channel channel, String queueName) throws IOException {
        PublisherRabbitMQ result = new PublisherRabbitMQ(channel);
		channel.basicConsume(queueName, true, result);
		return result;
	}
}
