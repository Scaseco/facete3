package org.hobbit.transfer;

import java.nio.ByteBuffer;

/**
 * Message layout is
 * [magic number] [stream id] [control message]
 *
 * @author raven
 *
 */
public class ChunkedProtocolControlSimple
    implements ChunkedProtocolControl
{
    public static final long MIN_MESSAGE_LENGTH = 13; // 8 bytes magic + 4 bytes stream id + 1 byte control message
    public static final long MAGIC_STREAM_CODE = 444888222;

    @Override
    public boolean isStreamControlMessage(ByteBuffer buffer) {
        boolean result;
        if(buffer.limit() < MIN_MESSAGE_LENGTH) {
            result = false;
        } else {
            long header = buffer.getLong(0);
            result = header == MAGIC_STREAM_CODE;
        }

        return result;
    }

    @Override
    public Object getStreamId(ByteBuffer buffer) {
        int result = buffer.getInt(8);
        return result;
    }

    @Override
    public StreamControl getMessageType(ByteBuffer buffer) {
        byte code = buffer.get(12);
        StreamControl result;
        switch(code) {
        case 1: result = StreamControl.READING_ABORTED; break;
        default: throw new RuntimeException("Message not understood");
        }

        return result;
    }

    @Override
    public ByteBuffer write(ByteBuffer buffer, Object streamId, byte message) {
        buffer.rewind();
        buffer.putLong(MAGIC_STREAM_CODE);
        buffer.putInt(((Number)streamId).intValue());
        buffer.put(message);
        return buffer;
    }

}
