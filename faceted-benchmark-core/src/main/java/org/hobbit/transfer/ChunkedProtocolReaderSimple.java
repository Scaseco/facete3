package org.hobbit.transfer;

import java.nio.ByteBuffer;

/**
 * Message schema:
 * [long: MAGIC_STREAM_CODE] [int: stream id] [long: sequence id] [payload]
 *
 *
 *
 * @author raven
 *
 */
public class ChunkedProtocolReaderSimple
    implements ChunkedProtocolReader
{
    protected long MAGIC_STREAM_CODE = 666999333;

    @Override
    public boolean isStreamRecord(ByteBuffer byteBuffer) {
        boolean result;
        if(byteBuffer.limit() < 16) {
            result = false;
        } else {
            long header = byteBuffer.getLong(0);
            result = header == MAGIC_STREAM_CODE;
        }

        return result;
    }

    public Object getStreamId(ByteBuffer byteBuffer) {
        long streamId = byteBuffer.getLong(8);
        return streamId;
    }

    @Override
    public Object getChunkId(ByteBuffer byteBuffer) {
        long chunkId = byteBuffer.getLong(12);
        if(chunkId < 0) {
            chunkId = -chunkId;
        }
        return chunkId;
    }

    @Override
    public boolean isStartOfStream(ByteBuffer byteBuffer) {
        long chunkId = byteBuffer.getLong(12);
        boolean result = chunkId == 1;
        return result;
    }


    @Override
    public boolean isLastChunk(ByteBuffer byteBuffer) {
        long chunkId = byteBuffer.getLong(12);
        boolean result = chunkId < 0;
        return result;
    }

    @Override
    public ByteBuffer getPayload(ByteBuffer byteBuffer) {
        byteBuffer.position(16);
        return byteBuffer;
    }


}
