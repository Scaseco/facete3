package org.hobbit.transfer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Publisher which puts messages on a queue
 * which is processed in separate thread.
 *
 * As a separate thread is used, debugging may be more cumbersome.
 *
 * a.subscribe((x) -> sendToB(...))
 * b.subscribe((x) -> sendToA(...))
 *
 * sendToA(...)
 *
 * @author raven Sep 22, 2017
 *
 */

public class PublishingWritableByteChannelQueued
    implements PublishingWritableByteChannel, Consumer<ByteBuffer>
{
    protected Collection<Consumer<? super ByteBuffer>> subscribers = Collections.synchronizedList(new ArrayList<>());

    protected BlockingQueue<ByteBuffer> queue = new LinkedBlockingQueue<>();
    protected ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PublishingWritableByteChannelQueued() {
        super();

        executorService.execute(() -> {
            while(!Thread.interrupted()) {
                ByteBuffer src;
                try {
                    src = queue.take();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                for(Consumer<? super ByteBuffer> subscriber : IterableUtils.synchronizedCopy(subscribers)) {
                    ByteBuffer tmp = src.duplicate();
                    subscriber.accept(tmp);
                }
            }
        });
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int result = src.remaining();
        accept(src);
        return result;
    }

    @Override
    public void accept(ByteBuffer src) {
        try {
            queue.put(src);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
