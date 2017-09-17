package org.hobbit.transfer;

import java.nio.ByteBuffer;

public class ChunkedProtocolWriterSimple
    implements ChunkedProtocolWriter
{
    public static final long MAGIC_STREAM_CODE = 666999333;

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
}
