package org.hobbit.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public abstract class InputStreamChunkedTransfer
    extends InputStream
{
    // Modifying these fields directly should be considered a hack
    protected boolean lastBatchSeen = false;
    protected BlockingQueue<ByteBuffer> clientQueue = new LinkedBlockingQueue<>();
    protected ByteBuffer currentBuffer;

    //protected ChunkedProtocolReader protocol;

    public InputStreamChunkedTransfer() throws IOException {
        super();
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

    @Override
    public int read() throws IOException {
        byte tmp[] = {0};
        int code = read(tmp, 0, 1);
        int result = code > 0 ? tmp[0] : -1;
        return result;
    }

}
