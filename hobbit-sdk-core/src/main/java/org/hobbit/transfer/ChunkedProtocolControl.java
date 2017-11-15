package org.hobbit.transfer;

import java.nio.ByteBuffer;

/**
 * Interface for simple stream protocols over byte channels
 *
 * FIXME Replace Object with something on the byte level - either
 * long (but then we won't be able to represent strings as ids),
 * or ByteBuffer - yield a bytebuffer with the bytes that make up the id
 *
 * The second option is the most promising one
 *
 * @author raven Sep 20, 2017
 *
 */
public interface ChunkedProtocolControl {

    boolean isStreamControlMessage(ByteBuffer buffer);
    // The stream id the control message applies to
    Object getStreamId(ByteBuffer buffer);

    StreamControl getMessageType(ByteBuffer buffer);

    ByteBuffer write(ByteBuffer buffer, Object StreamId, byte message);
}
