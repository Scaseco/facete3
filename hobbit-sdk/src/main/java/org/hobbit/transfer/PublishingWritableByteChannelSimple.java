package org.hobbit.transfer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PublishingWritableByteChannelSimple
    implements PublishingWritableByteChannel
{
    protected List<Consumer<? super ByteBuffer>> subscribers = new ArrayList<>();

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        for(Consumer<? super ByteBuffer> subscriber : subscribers) {
            //new Thread(() -> {
                ByteBuffer tmp = src.duplicate();
                subscriber.accept(tmp);
            //}).start();
        }
        return src.position();
    }

    @Override
    public Runnable subscribe(Consumer<? super ByteBuffer> observer) {
        this.subscribers.add(observer);
        return () -> unsubscribe(observer);
    }

    @Override
    public void unsubscribe(Consumer<? super ByteBuffer> observer) {
        this.subscribers.remove(observer);
    }

}
