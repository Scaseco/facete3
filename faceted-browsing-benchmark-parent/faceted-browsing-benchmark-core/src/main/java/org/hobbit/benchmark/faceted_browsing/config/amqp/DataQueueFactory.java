package org.hobbit.benchmark.faceted_browsing.config.amqp;

import java.nio.ByteBuffer;

import org.reactivestreams.Subscriber;

import com.rabbitmq.client.Channel;

import io.reactivex.Flowable;

public interface DataQueueFactory {
    Subscriber<ByteBuffer> createSender(Channel channel, String queueName) throws Exception;
    Flowable<ByteBuffer> createReceiver(Channel channel, String queueName) throws Exception;	    
}