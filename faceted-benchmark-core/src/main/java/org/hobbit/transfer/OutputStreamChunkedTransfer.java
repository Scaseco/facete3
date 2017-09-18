package org.hobbit.transfer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * OutputStream backed by a RabbitMQ channel.
 *
 * Buffers data in a {@link ByteBuffer} which is flushed whenever full.
 * The byte buffer's first long value is a sequence id which is incremented on every flush.
 * A negative sequence id indicates the last batch.
 *
 * @author Claus Stadler 2017-09-17
 *
 */
public class OutputStreamChunkedTransfer
    extends OutputStream
{
    protected ByteBuffer dataBuffer = ByteBuffer.allocate(4096);
    protected long batchSequenceId = 1;
    protected boolean closeChannelOnClose = false;

    protected ChunkedProtocolWriter protocol;
    protected Consumer<ByteBuffer> dataDelegate;

    protected Runnable closeAction;

    protected ByteBuffer payloadRegion;

    public OutputStreamChunkedTransfer(ChunkedProtocolWriter protocol, Consumer<ByteBuffer> dataDelegate, Runnable closeAction) {
        super();
        this.protocol = protocol;
        this.dataDelegate = dataDelegate;
        this.closeAction = closeAction;

        dataBuffer = protocol.nextBuffer(dataBuffer);
        //payloadRegion = protocol.getPayload(dataBuffer);
    }


    public static OutputStreamChunkedTransfer newInstanceForByteArrayDelegate(
            ChunkedProtocolWriter protocol,
            Consumer<byte[]> dataDelegate,
            Runnable closeAction) {
        OutputStreamChunkedTransfer result = new OutputStreamChunkedTransfer(
                new ChunkedProtocolWriterSimple(666),
                md -> {
                    try {
                        int pos = md.position();
                        System.out.println("pos = " + pos);
                        byte[] msgData = new byte[pos];
                        md.rewind();
                        md.get(msgData);
                        md.position(pos); // Reset position because we are nice
                        System.out.println("data = " + Arrays.toString(msgData));
                        dataDelegate.accept(msgData);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                null);

        return result;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {

        int remainingLen = len;
        while(remainingLen > 0) {
            int remainingCapacity = dataBuffer.limit() - dataBuffer.position();

            if(remainingCapacity == 0) {
                flush();
                continue;
            }

            int batchSize = Math.min(remainingLen, remainingCapacity);

            dataBuffer.put(b, off, batchSize);

            off += batchSize;
            remainingLen -= batchSize;
            remainingCapacity -= batchSize;
        }

    }

    @Override
    public void write(int b) throws IOException {
        write(new byte[]{(byte)b}, 0, 1);
    }

    @Override
    public void flush() throws IOException {
//        int pos = dataBuffer.position();

//        dataBuffer.rewind();
//        ByteBuffer slice = dataBuffer.slice();
//        slice.limit(pos);
//        ByteBuffer p
//        byte[] msgData = new byte[pos];
//        dataBuffer.rewind();
//        dataBuffer.get(msgData);

        System.out.println("Sending packet: " + protocol.toString(dataBuffer));
        //channel.basicPublish(exchangeName, routingKey, properties, msgData);
        dataDelegate.accept(dataBuffer);

        dataBuffer = protocol.nextBuffer(dataBuffer);
        //payloadRegion = protocol.getPayload(dataBuffer);


        //super.flush();
    }


    @Override
    public void close() throws IOException {
        protocol.setLastChunkFlag(dataBuffer, true);

//    	int pos = dataBuffer.position();
//        dataBuffer.position(0);
//        long currentSeqId = dataBuffer.getLong(0);
//        dataBuffer.position(0);
//        dataBuffer.putLong(0, -currentSeqId);
//        dataBuffer.position(pos);

        flush();

        if(closeAction != null) {
            closeAction.run();
        }
//        if(closeChannelOnClose) {
//            try {
//                channel.close();
//            } catch (TimeoutException e) {
//                throw new RuntimeException(e);
//            }
//        }
    }
}
