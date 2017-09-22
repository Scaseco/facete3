package org.hobbit.transfer.rabbitmq;

import java.util.function.Consumer;

import org.hobbit.transfer.Publisher;

import javassist.bytecode.ByteArray;

public class PublisherRabbitMqChannel
    implements Publisher<ByteArray>
{

    // TODO Implement

    @Override
    public Runnable subscribe(Consumer<? super ByteArray> subscriber) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unsubscribe(Consumer<? super ByteArray> subscribe) {
        // TODO Auto-generated method stub

    }

}
