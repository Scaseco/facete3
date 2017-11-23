package org.hobbit.transfer;

import java.nio.ByteBuffer;

public interface ChunkedProtocolReader<T> {
    /**
     * Check whether the record can be handled by the protocol
     * @param byteBuffer
     * @return
     */
    boolean isStreamRecord(T byteBuffer);

    /**
     * Read the id of the stream
     *
     * @param byteBuffer
     * @return
     */
    Object getStreamId(T byteBuffer);

    /**
     * Get the sequence id of the chunk within the stream
     *
     * @param byteBuffer
     * @return
     */
    Object getChunkId(T byteBuffer);

    /**
     * Check whether this is the first chunk of a stream.
     * Do not make any assumptions about chunk ids.
     *
     * @param byteBuffer
     * @return
     */
    boolean isStartOfStream(T byteBuffer);



    boolean isLastChunk(T byteBuffer);

    /**
     * Adjust the byte buffer to the payload.
     *
     *
     * @param byteBuffer
     * @return
     */
    T getPayload(T byteBuffer);

}
