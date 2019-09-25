package org.hobbit.transfer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


public class ReadableByteChannelSimple
    implements ReadableByteChannel, Subscriber<ByteBuffer>
{
    protected volatile boolean lastBatchSeen = false;
    protected BlockingQueue<ByteBuffer> clientQueue = new LinkedBlockingQueue<>();
    protected ByteBuffer currentBuffer;


    protected Throwable abortException = null;

    protected boolean isOpen = true;
    protected Runnable closeAction;

    public ReadableByteChannelSimple() {
        super();
    }

    public ReadableByteChannelSimple(Runnable closeAction) {
        super();
        this.closeAction = closeAction;
    }

    public boolean isComplete() {
        return lastBatchSeen;
    }

    @Override
    public void onSubscribe(Subscription s) {
    }
    
    @Override
    public void onComplete() {
    	System.out.println("OnComplete called");
        synchronized(this) {
            lastBatchSeen = true;
            notifyAll();
        }
    }

    @Override
    public void onNext(ByteBuffer bodyBuffer) {
        synchronized(this) {
        	bodyBuffer.mark();
        	byte[] tmp = new byte[bodyBuffer.remaining()];
        	bodyBuffer.get(tmp, 0, bodyBuffer.remaining());
        	//String s = new String(tmp, StandardCharsets.UTF_8);
        	bodyBuffer.reset();
        	
        	//System.out.println("Appending to queue: " + s);
            try {
				clientQueue.put(bodyBuffer);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
        }
    }

    /**
     * If the stream is already closed, this is a noop.
     *
     * Otherwise, sets the abort exception and interrupts any wait for data
     *
     */
    @Override
    public void onError(Throwable t) {
        synchronized(this) {
            if(!lastBatchSeen) {
                abortException = t;
                notifyAll();
            }
        }
    }


    /**
     * Supply data to the channel, which will be returned by read operations in sequence
     *
     * @param data
     */
//    public void supplyData(ByteBuffer data) {
//        clientQueue.add(data);
//    }



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
        //int readBytes = 0;

        while(remaining > 0) {
            int available = currentBuffer == null ? 0 : currentBuffer.remaining();

            if(available == 0) {
                synchronized(this) {
                    // If we are at the last batch and have not read anything yet, we have reached the end
                    if(lastBatchSeen && clientQueue.isEmpty()) {
                        if(result == 0) {
                            result = -1;
                        }
                        break;
                    } else {
                        try {
                            while(clientQueue.isEmpty() && !lastBatchSeen && abortException == null) {
                                //System.out.println("byte channel reader waiting");
                                wait();
                                //wait(1000);
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
//            System.out.println("Got:");
//            System.out.println(new String(dst.array(), StandardCharsets.UTF_8));

            currentBuffer.position(newOff);

            result += toRead;
            remaining -= toRead;
        }

        if(result == -1) {
            System.out.println(this.getClass().getName() + ": Read " + result + " bytes");
        }

        return result;
    }

}
