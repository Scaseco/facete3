package org.hobbit.transfer;

import java.nio.ByteBuffer;

public class ChunkedProtocolWriterSimple
    implements ChunkedProtocolWriter<ByteBuffer>
{
    public static final long MAGIC_STREAM_CODE = 0xfafafafababababal;//666999333;
    public static final int MIN_MESSAGE_LENGTH = 20;

    protected int streamId;
    protected long sequenceId = 1;

    public ChunkedProtocolWriterSimple() {
        this(-1);
    }

    public ChunkedProtocolWriterSimple(int streamId) {
        this.streamId = streamId;
    }


//    @Override
//    public ByteBuffer initBufferForNewStream(ByteBuffer dataBuffer) {
//        nextBuffer(dataBuffer, false);
//        return dataBuffer;
//    }


    @Override
    public boolean setLastChunkFlag(ByteBuffer buffer, boolean flag) {
        long sequenceId = buffer.getLong(12);
        long newSequenceId = flag && sequenceId > 0
                ? -sequenceId
                : !flag && sequenceId < 0
                    ? -sequenceId
                    : sequenceId;

        buffer.putLong(12, newSequenceId);
        boolean result = sequenceId == newSequenceId;
        return result;
    }

    @Override
    public ByteBuffer nextBuffer(ByteBuffer previousBuffer) {
        previousBuffer.clear();
        previousBuffer.rewind();
        previousBuffer.putLong(MAGIC_STREAM_CODE);
        previousBuffer.putInt(streamId);
        previousBuffer.putLong(sequenceId++);

        return previousBuffer;
    }

    public String toString(ByteBuffer buffer) {
        String result = "[" +
                buffer.getLong(0) + ", " +
                buffer.getInt(8) + ", " +
                buffer.getLong(12) + ", " +
                // .limit()
                (buffer.position() - 20) + " bytes of payload]";

        return result;
    }

//	@Override
//	public ByteBuffer getPayloadRegion(ByteBuffer buffer) {
//		buffer.slice().position(20);
//	}
}
