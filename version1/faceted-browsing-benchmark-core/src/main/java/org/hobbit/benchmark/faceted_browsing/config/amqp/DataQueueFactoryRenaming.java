package org.hobbit.benchmark.faceted_browsing.config.amqp;

import java.nio.ByteBuffer;
import java.util.function.Function;

import org.reactivestreams.Subscriber;

import com.rabbitmq.client.Channel;

import io.reactivex.Flowable;

public class DataQueueFactoryRenaming
    implements DataQueueFactory
{
    protected DataQueueFactory delegate;
    protected Function<String, String> queueNameMapper;
    
    public DataQueueFactoryRenaming(DataQueueFactory delegate,
            Function<String, String> queueNameMapper) {
        super();
        this.delegate = delegate;
        this.queueNameMapper = queueNameMapper;
    }

    @Override
    public Subscriber<ByteBuffer> createSender(Channel channel, String baseQueueName) throws Exception {
        String queueName = queueNameMapper.apply(baseQueueName);
        return delegate.createSender(channel, queueName);            
    }

    @Override
    public Flowable<ByteBuffer> createReceiver(Channel channel, String baseQueueName) throws Exception {
        String queueName = queueNameMapper.apply(baseQueueName);
        return delegate.createReceiver(channel, queueName);            
    }
}