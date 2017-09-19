package org.hobbit.transfer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class ReadableByteChannelSimple
    implements ReadableByteChannel
{
    protected volatile boolean lastBatchSeen = false;
    protected BlockingQueue<ByteBuffer> clientQueue = new LinkedBlockingQueue<>();
    protected ByteBuffer currentBuffer;


    protected Throwable abortException = null;

    protected boolean isOpen = true;
    protected Runnable closeAction;

    public ReadableByteChannelSimple(Runnable closeAction) {
        super();
        this.closeAction = closeAction;
    }

    protected void setLastBatchSeen() {
        synchronized(this) {
            this.lastBatchSeen = true;
            notifyAll();
        }
    }

    protected void appendDataToQueue(ByteBuffer bodyBuffer) throws InterruptedException {
        synchronized(this) {
            clientQueue.put(bodyBuffer);
        }
    }

    /**
     * If the stream is already closed, this is a noop.
     *
     * Otherwise, sets the abort exception and interrupts any wait for data
     *
     */
    public  void abort(Throwable t) {
        if(!lastBatchSeen) {
            abortException = t;
            notifyAll();
        }
    }


    /**
     * Supply data to the channel, which will be returned by read operations in sequence
     *
     * @param data
     */
    public  void supplyData(ByteBuffer data) {
        clientQueue.add(data);
    }



    // AsynchronousByteChannel methods

    @Override
    public void close() throws IOException {
        if(closeAction != null) {
            closeAction.run();
        }
        isOpen = false;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }


    @Override
    public  int read(ByteBuffer dst) {
        int result = 0;

        int remaining = dst.remaining();
        int readBytes = 0;

        while(remaining > 0) {
            int available = currentBuffer == null ? 0 : currentBuffer.remaining();

            if(available == 0) {
                synchronized(this) {
                    // If we are at the last batch and have not read anything yet, we have reached the end
                    if(lastBatchSeen && clientQueue.isEmpty()) {
                        if(readBytes == 0) {
                            result = -1;
                        }
                        break;
                    } else {
                        try {
                            while(clientQueue.isEmpty() && !lastBatchSeen) {
                                wait();
                            }

                            currentBuffer = clientQueue.poll();
                            //currentBuffer = clientQueue.take();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        if(abortException != null) {
                            throw new RuntimeException(abortException);
                        }

                        continue;
                    }
                }

            }

            int toRead = Math.min(remaining, available);

            int off = currentBuffer.position();
            int newOff = off + toRead;

            ByteBuffer tmp = currentBuffer.duplicate();
            tmp.limit(newOff);
            dst.put(tmp);
            currentBuffer.position(newOff);

            result += toRead;
            remaining -= toRead;
        }

        System.out.println("Read " + result + " bytes");
        return result;
    }

}
