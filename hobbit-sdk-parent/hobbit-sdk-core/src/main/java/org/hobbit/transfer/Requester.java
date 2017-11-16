package org.hobbit.transfer;

import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class Requester
    implements Function<ByteBuffer, CompletableFuture<ByteBuffer>>
{
    protected WritableByteChannel channel;

    @Override
    public CompletableFuture<ByteBuffer> apply(ByteBuffer t) {

        return null;
    }

}
