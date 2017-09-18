package org.hobbit.transfer;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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
    protected ChunkedProtocolReader readProtocol;
    protected ChunkedProtocolControl controlProtocol;

    protected Map<Object, InputStreamChunkedTransfer> openIncomingStreams = new HashMap<>();
    protected List<Consumer<InputStream>> callbacks = new ArrayList<>();

    protected Consumer<ByteBuffer> controlChannel;

    public InputStreamManagerImpl(Consumer<ByteBuffer> controlChannel) {
        this.controlChannel = controlChannel;


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
            InputStreamChunkedTransfer stream = openIncomingStreams.get(streamId);

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

            InputStreamChunkedTransfer in = openIncomingStreams.get(streamId);

            if(in == null) {
                boolean isStartOfStream = readProtocol.isStartOfStream(buffer);

                if(isStartOfStream) {
                    InputStreamChunkedTransfer tmp = new InputStreamChunkedTransfer(() -> {
                        ByteBuffer readingAbortedMessage = controlProtocol.write(ByteBuffer.allocate(16), streamId, StreamControl.READING_ABORTED.getCode());
                        controlChannel.accept(readingAbortedMessage);
                    });
                    in = tmp;
                    openIncomingStreams.put(streamId, in);

                    for(Consumer<InputStream> callback : callbacks) {
                        CompletableFuture.runAsync(() -> callback.accept(tmp));
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
    public void registerCallback(Consumer<InputStream> callback) {
        callbacks.add(callback);
    }

    @Override
    public void unregisterCallback(Consumer<InputStream> callback) {
        callbacks.remove(callback);
    }

}
