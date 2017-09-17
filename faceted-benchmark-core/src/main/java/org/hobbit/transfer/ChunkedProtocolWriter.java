package org.hobbit.transfer;

import java.nio.ByteBuffer;

/**
 * A chunked protocol handles the metadata of byte buffers for sending
 * a payload in multiple chunks.
 *
 * Protocol instances are stateful (e.g. stream id and chunk id), hence,
 * every operation of sending a complete payload needs its own protocol instance.
 *
 *
 *
 * @author raven
 *
 */
public interface ChunkedProtocolWriter {

//    /**
//     * Within the current bounds, write the metadata
//     * and adjust the byte buffer to the bounds for the payload
//     *
//     *
//     * @param byteBuffer
//     * @return
//     */
//    ByteBuffer initBufferForNewStream(ByteBuffer byteBuffer);

    /**
     * Update the byte buffer for the next metadata for the next payload
     * Adjust bounds to the payload segment
     *
     * @param previousByteBuffer
     * @param isLastChunk
     * @return
     */
    ByteBuffer nextBuffer(ByteBuffer previousBuffer);

    /**
     *
     * @param buffer
     * @param flag
     * @return True iff the flag was modified
     */
    boolean setLastChunkFlag(ByteBuffer buffer, boolean flag);

//    /**
//     * Set up a byte buffer to hold the next payload.
//     * The returned ByteBuffer is intended for immediate use.
//     * Subsequent calls may return the same buffer.
//     *
//     * @return
//     */
//    ByteBuffer nextBuffer(ByteBuffer previousByteBuffer, boolean isLastChunk);
}
