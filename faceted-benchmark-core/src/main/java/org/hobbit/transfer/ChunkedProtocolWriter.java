package org.hobbit.transfer;

import java.nio.ByteBuffer;

public interface ChunkedProtocolWriter {
    /**
     * Set up a byte buffer to hold the next payload.
     * The returned ByteBuffer is intended for immediate use.
     * Subsequent calls may return the same buffer.
     *
     * @return
     */
    ByteBuffer nextBuffer(boolean isLastChunk);
}
