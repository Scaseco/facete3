package org.hobbit.transfer;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class StreamManagerImpl
    implements StreamManager
{
    protected ExecutorService executorService;
    protected ChunkedProtocolReader protocol;

    protected Map<Object, InputStreamChunkedTransfer> openStreams;

    protected Consumer<InputStream> callback;

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

        // If there is a new stream
        Future<?> future = executorService.submit(() -> callback.accept(in));

        // TODO At some point we need to inspect the future; even if only
        // to check for any exceptions

        try {
            in.appendDataToQueue(payload);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

}
