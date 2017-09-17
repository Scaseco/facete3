package org.hobbit.transfer;

import java.nio.ByteBuffer;

public interface ChunkedProtocolReader {
    /**
     * Check whether the record can be handled by the protocol
     * @param byteBuffer
     * @return
     */
    boolean isStreamRecord(ByteBuffer byteBuffer);

    /**
     * Read the id of the stream
     *
     * @param byteBuffer
     * @return
     */
    Object getStreamId(ByteBuffer byteBuffer);

    /**
     * Get the sequence id of the chunk within the stream
     *
     * @param byteBuffer
     * @return
     */
    Object getChunkId(ByteBuffer byteBuffer);

    /**
     * Check whether this is the first chunk of a stream.
     * Do not make any assumptions about chunk ids.
     *
     * @param byteBuffer
     * @return
     */
    boolean isStartOfStream(ByteBuffer byteBuffer);



    boolean isLastChunk(ByteBuffer byteBuffer);

    /**
     * Adjust the byte buffer to the payload.
     *
     *
     * @param byteBuffer
     * @return
     */
    ByteBuffer getPayload(ByteBuffer byteBuffer);
}
