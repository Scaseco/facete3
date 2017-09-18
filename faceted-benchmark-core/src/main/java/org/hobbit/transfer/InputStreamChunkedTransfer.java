package org.hobbit.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class InputStreamChunkedTransfer
    extends InputStream
{
    // Modifying these fields directly should be considered a hack
    protected boolean lastBatchSeen = false;
    protected BlockingQueue<ByteBuffer> clientQueue = new LinkedBlockingQueue<>();
    protected ByteBuffer currentBuffer;


    protected Throwable abortException = null;

    protected Runnable closeAction;
    //protected ChunkedProtocolReader protocol;

    public InputStreamChunkedTransfer(Runnable closeAction) {
        super();
        this.closeAction = closeAction;
    }

    protected void setLastBatchSeen() {
        this.lastBatchSeen = true;
    }

    protected void appendDataToQueue(ByteBuffer bodyBuffer) throws InterruptedException {
        clientQueue.put(bodyBuffer);
    }


    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int remaining = len;
        int result = 0;

        while(remaining > 0) {
            int available = currentBuffer == null ? 0 : currentBuffer.remaining();
            if(available == 0) {
                // If we are at the last batch and have not read anything yet, we have reached the end
                if(lastBatchSeen && clientQueue.isEmpty()) {
                    if(result == 0) {
                        result = -1;
                    }
                    break;
                } else {
                    try {
                        currentBuffer = clientQueue.take();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if(abortException != null) {
                        throw new RuntimeException(abortException);
                    }

                    continue;
                }
            }

            int toRead = Math.min(remaining, available);
            result += toRead;
            currentBuffer.get(b, off, toRead);
            off += toRead;
            remaining -= toRead;
        }

        return result;
    }

    /**
     * If the stream is already closed, this is a noop.
     *
     * Otherwise, sets the abort exception and interrupts any wait for data
     *
     */
    public void abort(Throwable t) {
        if(!lastBatchSeen) {
            abortException = t;
            clientQueue.notifyAll();
        }
    }

    @Override
    public int read() throws IOException {
        byte tmp[] = {0};
        int code = read(tmp, 0, 1);
        int result = code > 0 ? tmp[0] : -1;
        return result;
    }

    @Override
    public void close() throws IOException {
        if(closeAction != null) {
            closeAction.run();
        }

        super.close();
    }
}
