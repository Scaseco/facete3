package org.hobbit.transfer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

public class PublishingWritableByteChannelSimple
    implements PublishingWritableByteChannel, Consumer<ByteBuffer>
{
    protected Collection<Consumer<? super ByteBuffer>> subscribers = Collections.synchronizedList(new ArrayList<>());

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int result = src.remaining();
        accept(src);
        return result;
    }

    @Override
    public void accept(ByteBuffer src) {
        for(Consumer<? super ByteBuffer> subscriber : IterableUtils.synchronizedCopy(subscribers)) {
            ByteBuffer tmp = src.duplicate();
            subscriber.accept(tmp);
        }
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

