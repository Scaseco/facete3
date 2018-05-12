package org.hobbit.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream;
import com.google.common.io.ByteStreams;


/**
 * The handler holds an internal future which is resolved once the appropriate messages have been received
 *
 * @author raven
 *
 */
interface ByteChannelRequestResponseHandler
    extends Consumer<ByteBuffer>, Supplier<Future<InputStream>>, AutoCloseable
{

}

/**
 * A simple ByteBufferRequest:
 *
 * It waits for an incoming byte buffer which satisfies the predicate
 *
 * @author raven
 *
 */
class SimpleByteChannelRequestResponseHandler
    implements Consumer<ByteBuffer>, Supplier<Future<InputStream>>, AutoCloseable
{
    protected Predicate<ByteBuffer> responseCondition;
    //protected Function<ByteBuffer, ByteBuffer> ;

    protected CompletableFuture<InputStream> future = new CompletableFuture<>();

    public SimpleByteChannelRequestResponseHandler(Predicate<ByteBuffer> responseCondition) {
        super();
        this.responseCondition = responseCondition;
    }

    @Override
    public void accept(ByteBuffer buffer) {
        boolean isResponse = responseCondition.test(buffer);
        if(isResponse) {
            InputStream in = new ByteBufferBackedInputStream(buffer);
            future.complete(in);
        }
    }

    @Override
    public CompletableFuture<InputStream> get() {
        return future;
    }

    @Override
    public void close() throws Exception {

    }
}


/**
 * A simple request protocol which sends a
 * ByteBuffer to a channel, and waits for a predicate on any of the publishers to become true
 *
 * @author raven
 *
 */
public class ByteChannelRequestFactorySimple
    extends ByteChannelRequestFactoryBase
{
    protected Predicate<ByteBuffer> testByteBufferForResponse;


    public ByteChannelRequestFactorySimple(ByteChannel dataChannel, Collection<Publisher<? extends ByteBuffer>> publishers, Predicate<ByteBuffer> testByteBufferForResponse) {
        super(dataChannel, publishers);
        this.testByteBufferForResponse = testByteBufferForResponse;
    }

    public CompletableFuture<InputStream> sendRequestAndAwaitResponse(InputStream inputStream, long timeout, TimeUnit unit) throws IOException {
        ByteBuffer msg;
        try {
            msg = ByteBuffer.wrap(ByteStreams.toByteArray(inputStream));
        } catch(Exception e) {
            throw new RuntimeException();
        }

        SimpleByteChannelRequestResponseHandler request = new SimpleByteChannelRequestResponseHandler(testByteBufferForResponse);

        // Register to all publishers to obtain the response to this request
        List<Runnable> unsubscribers = publishers.stream()
                .map(publisher -> publisher.subscribe(request))
                .collect(Collectors.toList());


        dataChannel.write(msg);

        CompletableFuture<InputStream> result = request.get();

        // Regardless of how the request ends, deregister it from the listeners
        result.whenComplete((i, s) -> unsubscribers.forEach(Runnable::run));

        return result;
    }
}
