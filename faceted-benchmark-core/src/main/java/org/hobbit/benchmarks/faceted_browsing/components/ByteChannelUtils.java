package org.hobbit.benchmarks.faceted_browsing.components;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.hobbit.transfer.Publisher;

public class ByteChannelUtils {
    public static CompletableFuture<ByteBuffer> sendMessageAndAwaitRepsonse(WritableByteChannel dataChannel, ByteBuffer msg, Collection<Publisher<ByteBuffer>> publishers, Predicate<ByteBuffer> responseCondition) throws IOException {
        CompletableFuture<ByteBuffer> result = PublisherUtils.awaitMessage(publishers, responseCondition);

        try {
            dataChannel.write(msg);
        } catch(Exception e) {
            result.completeExceptionally(e);
            throw new RuntimeException(e);
        }

        return result;
    }

}
