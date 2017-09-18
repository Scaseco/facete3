package org.hobbit.transfer;

import java.nio.ByteBuffer;

public interface ChunkedProtocolControl {

    boolean isStreamControlMessage(ByteBuffer buffer);
    // The stream id the control message applies to
    Object getStreamId(ByteBuffer buffer);

    StreamControl getMessageType(ByteBuffer buffer);

    ByteBuffer write(ByteBuffer buffer, Object StreamId, byte message);
}
