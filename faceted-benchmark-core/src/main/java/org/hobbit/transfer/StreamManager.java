package org.hobbit.transfer;

import java.nio.ByteBuffer;

public interface StreamManager {
    boolean isStartOfNewStream(ByteBuffer data);
    //InputStream startStream(byte[] data);
    boolean handle(ByteBuffer data);

    //void registerCallback(Consumer<InputStream> callback);
}
