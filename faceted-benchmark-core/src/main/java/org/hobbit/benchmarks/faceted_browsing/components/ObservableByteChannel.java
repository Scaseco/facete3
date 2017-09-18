package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ObservableByteChannel
    implements WritableByteChannel
{
    protected List<Consumer<ByteBuffer>> observers = new ArrayList<>();

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        for(Consumer<ByteBuffer> listener : observers) {
            ByteBuffer tmp = src.duplicate();
            listener.accept(tmp);
        }
        return src.position();
    }

    public void addObserver(Consumer<ByteBuffer> observer) {
        this.observers.add(observer);
    }

    public void removeObserver(Consumer<ByteBuffer> observer) {
        this.observers.remove(observer);
    }
}
