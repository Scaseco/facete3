package org.hobbit.transfer;

import java.nio.ByteBuffer;
import java.util.Map;

public class StreamManagerImpl
    implements StreamManager
{
    protected ChunkedProtocolReader protocol;

    protected Map<Object, InputStreamChunkedTransfer> openStreams;

    @Override
    public boolean isStartOfNewStream(ByteBuffer byteBuffer) {
        boolean result = protocol.isStartOfStream(byteBuffer);
        return result;
    }

    @Override
    public boolean handle(ByteBuffer byteBuffer) {
        // Check whether the data is a stream chunk, and if so, to which stream it belongs

        Object streamId = protocol.getStreamId(byteBuffer);

        ByteBuffer payload = protocol.getPayload(byteBuffer);
        InputStreamChunkedTransfer in = openStreams.get(streamId);
        in.appendDataToQueue(payload);

    }
}
