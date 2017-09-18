package org.hobbit.transfer;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

public interface StreamManager {
    //OutputStream newOutputStream();

    //boolean isStartOfNewStream(ByteBuffer data);
    //InputStream startStream(byte[] data);

    /**
     * Inspects incoming data, which may be either data chunks
     * or control events
     * Gracefully ignores unrelated data
     *
     * @param data
     * @return
     */
    boolean handleIncomingData(ByteBuffer data);

    /**
     * Register a callback whenever a new stream is encountered
     */
    void registerCallback(Consumer<InputStream> callback);
    void unregisterCallback(Consumer<InputStream> callback);
}
