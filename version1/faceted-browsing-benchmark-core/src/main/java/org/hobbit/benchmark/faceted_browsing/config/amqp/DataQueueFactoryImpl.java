package org.hobbit.benchmark.faceted_browsing.config.amqp;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;

import org.hobbit.core.config.RabbitMqFlows;
import org.reactivestreams.Subscriber;

import com.rabbitmq.client.Channel;

import io.reactivex.Flowable;

public class DataQueueFactoryImpl
           implements DataQueueFactory
    {
       @Override
       public Subscriber<ByteBuffer> createSender(Channel channel, String queueName) throws IOException {
           return RabbitMqFlows.createDataSender(channel, queueName);           
       }

       @Override
       public Flowable<ByteBuffer> createReceiver(Channel channel, String queueName) throws IOException, TimeoutException {
           return RabbitMqFlows.createDataReceiver(channel, queueName);           
       }
   }