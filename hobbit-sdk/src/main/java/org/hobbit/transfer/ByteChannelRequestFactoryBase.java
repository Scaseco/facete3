package org.hobbit.transfer;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Collection;
import java.util.function.Supplier;

public class ByteChannelRequestFactoryBase {
    protected ByteChannel dataChannel;
    protected Collection<? extends Publisher<? extends ByteBuffer>> publishers;

    protected Supplier<? extends ChunkedProtocolControl> protocolFactory;

    public ByteChannelRequestFactoryBase(ByteChannel dataChannel, Collection<? extends Publisher<? extends ByteBuffer>> publishers) {
        super();
        this.dataChannel = dataChannel;
        this.publishers = publishers;
    }


    /**
     * Sends a request in the form of an input stream over a byte channel,
     * and returns a {@link CompletableFuture}
     * which resolves as soon as a response condition on any of the publishers is encountered.
     *
     * It is recommended to always close the input stream of the response,
     * especially when one is not interested in its content.
     *
     * @param inputStream
     * @param timeout
     * @param unit
     * @return
     */
//    public CompletableFuture<InputStream> sendRequestAndAwaitResponse(InputStream inputStream, long timeout, TimeUnit unit) {
//        // Create an output stream over the data channel to send the request
//
//        // Create an object that encapsulates the request;
//        // Its main purpose is to listen to the publishers for specific responses
//        ByteChannelRequest request = new ByteChannelRequest();
//
//        // Add the condition of when a response to the request is seen
//        Predicate<ByteBuffer> responseCondition = (buffer) -> buffer.get(0) == Commands.BENCHMARK_FINISHED_SIGNAL;
//
//        Runnable<OutputStream> requestHandler =
//
//        // Response handler:
//        // Simple response handler: check the dataChannel for some condition. if it is met, just return the byte buffer
//        InputStream response = new ByteBufferBackedInputStream(data);
//
//        // Streaming response handler: Listen on the data channel for stream with appropriate id
//
//
//
//        // Register to all publishers to obtain the response to this request
//        List<Runnable> unsubscribers = publishers.stream()
//                .map(publisher -> publisher.subscribe(request))
//                .collect(Collectors.toList());
//
//
//        // Transfer the data
//        // TODO Can we have a dummy protocol that does not add any protocol metadata?
//        Protocol protocol = protocolFactory.get();
//        OutputStream out = OutputStreamChunkedTransfer.newInstanceForByteChannel(protocol, dataChannel, null);
//
//        FileCopyUtils.copy(inputStream, out);
//
//
//        // Sleep until a publisher's event wakes us up or we reach timeout
//
//    }


}

