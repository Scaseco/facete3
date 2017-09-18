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
    public ChunkedProtocolReaderSimple() {

    }

    @Override
    public boolean isStreamRecord(ByteBuffer byteBuffer) {
        boolean result;
        if(byteBuffer.limit() < ChunkedProtocolWriterSimple.MIN_MESSAGE_LENGTH) {
            result = false;
        } else {
            long header = byteBuffer.getLong(0);
            result = header == ChunkedProtocolWriterSimple.MAGIC_STREAM_CODE;
        }

        return result;
    }

    // TODO Object might be a bad idea here, as this would actually require a object<->byte encoder
    // If we do not want to commit to a specific stream id length, we could just
    // return the bytes (or byte region) of the id
    public Object getStreamId(ByteBuffer byteBuffer) {
        long streamId = byteBuffer.getInt(8);
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
        byteBuffer.position(ChunkedProtocolWriterSimple.MIN_MESSAGE_LENGTH);
        return byteBuffer;
    }

}
