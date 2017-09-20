package org.hobbit.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Implements input streams over a data channel.
 *
 *
 *
 * TODO Create a watch dog thread that cleans up stale streams
 *
 * @author raven
 *
 */
public class InputStreamManagerImpl
    implements StreamManager
{
    protected ExecutorService executorService = Executors.newCachedThreadPool(); //Executors.newCachedThreadPool();

    protected ChunkedProtocolReader readProtocol;
    protected ChunkedProtocolControl controlProtocol;

    protected Map<Object, ReadableByteChannelSimple> openIncomingStreams = new HashMap<>();
    protected Collection<Consumer<? super InputStream>> subscribers = Collections.synchronizedCollection(new ArrayList<>());

    protected Consumer<ByteBuffer> controlChannel;

    public InputStreamManagerImpl(Consumer<ByteBuffer> controlChannel) {
        this.controlChannel = controlChannel;


        this.readProtocol = new ChunkedProtocolReaderSimple();
        this.controlProtocol = new ChunkedProtocolControlSimple();
    }

    public InputStreamManagerImpl(WritableByteChannel controlChannel) {
        this.controlChannel = t -> {
            try {
                // Note: We assume that the channel always consumes all bytes
                controlChannel.write(t);
            } catch (IOException e) {
                throw new RuntimeException();
            }
        };


        this.readProtocol = new ChunkedProtocolReaderSimple();
        this.controlProtocol = new ChunkedProtocolControlSimple();
    }

    //this.writeProtocolFactory = (streamId) -> new ChunkedProtocolWriterSimple(streamId);
//    @Override
//    public OutputStream newOutputStream() {
//        int streamId = nextStreamId++;
//
//        ChunkedProtocolWriter writeProtocol = writeProtocolFactory.apply(streamId);
//        OutputStream result = new OutputStreamChunkedTransfer(writeProtocol, outputTransport, () -> {});
//        return result;
//    }

    @Override
    public boolean handleIncomingData(ByteBuffer buffer) {
        // Check for control event
        boolean isControlMessage = controlProtocol.isStreamControlMessage(buffer);


        if(isControlMessage) {
            Object streamId = controlProtocol.getStreamId(buffer);

            // Check if the stream is being handled
            ReadableByteChannelSimple stream = openIncomingStreams.get(streamId);

            if(stream != null) {
                StreamControl message = controlProtocol.getMessageType(buffer);
                switch(message){
                case READING_ABORTED:
                    stream.abort(new RuntimeException("reading aborted"));
                    break;
                default:
                    System.out.println("Unknown message type: " + message);
                    break;
                }

            }

        }

        // Check whether the data is a stream chunk, and if so, to which stream it belongs

        boolean isDataMessage = readProtocol.isStreamRecord(buffer);
        if(isDataMessage) {
            Object streamId = readProtocol.getStreamId(buffer);

            ReadableByteChannelSimple in = openIncomingStreams.get(streamId);

            if(in == null) {
                boolean isStartOfStream = readProtocol.isStartOfStream(buffer);

                if(isStartOfStream) {
                    ReadableByteChannelSimple tmp = new ReadableByteChannelSimple() {
                        @Override
                        public void close() throws IOException {
                            // If the byte channel is closed before completed, send a control message
                            // to hint any sender to stop sending the stream
                            if(!isDone()) {
                                ByteBuffer readingAbortedMessage = controlProtocol.write(ByteBuffer.allocate(16), streamId, StreamControl.READING_ABORTED.getCode());
                                controlChannel.accept(readingAbortedMessage);
                            }
                            super.close();
                        }
                    };
                    in = tmp;
                    openIncomingStreams.put(streamId, in);

                    // This is the event to a client that there exists a new input stream.
                    // This runs is a separate thread
                    for(Consumer<? super InputStream> subscriber : IterableUtils.synchronizedCopy(subscribers)) {

                        // TODO This could be a long running action - we should not occupy the fork/join pool
                        InputStream tmpIn = Channels.newInputStream(tmp);
                        //CompletableFuture.runAsync(() -> {
                        // TODO Get any exceptions from the executor service
                        executorService.submit(() -> {
                            try {
                                subscriber.accept(tmpIn);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        });

                        //});
                    }
                }
            }

            if(in != null) {
                ByteBuffer payload = readProtocol.getPayload(buffer);
                try {
                    boolean isLastChunk = readProtocol.isLastChunk(buffer);
                    if(isLastChunk) {
                        in.setLastBatchSeen();
                    }
                    in.appendDataToQueue(payload);
                } catch(InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return true;
    }

    @Override
    public Runnable subscribe(Consumer<? super InputStream> subscriber) {
        subscribers.add(subscriber);
        return () -> unsubscribe(subscriber);
    }

    @Override
    public void unsubscribe(Consumer<? super InputStream> subscriber) {
        subscribers.remove(subscriber);
    }

    @Override
    public void close() throws IOException {
        subscribers = Collections.emptyList();

        executorService.shutdown();
        try {
            executorService.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
