package org.hobbit.transfer;

import java.io.InputStream;
import java.nio.ByteBuffer;

public interface StreamManager
    extends Publisher<InputStream>
{
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
//    void subscribe(Consumer<? super InputStream> callback);
//    void unsubscribe(Consumer<InputStream> callback);
}
