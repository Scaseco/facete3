package org.hobbit.transfer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

class ConsumerSorting<T, S extends Comparable<S>>
	implements Consumer<T>
{
	protected Consumer<? super T> delegate;
	
	protected Function<? super T, ? extends S> getSeqId;
	protected Function<? super S, ? extends S> getNextSeqId;
	//protected DiscreteDomain<S> discreteDomain;

	protected S expectedSeqId;
	
	protected NavigableMap<S, T> seqIdToValue = new TreeMap<>();
	
	
	
	public ConsumerSorting(
			S expectedSeqId,
			Function<? super T, ? extends S> getSeqId,
			Function<? super S, ? extends S> getNextSeqId,
			Consumer<? super T> delegate) {
		super();
		this.getSeqId = getSeqId;
		this.getNextSeqId = getNextSeqId;
		this.expectedSeqId = expectedSeqId;
		this.delegate = delegate;
	}



	public synchronized void accept(T value) {
		S seqId = getSeqId.apply(value);

		// TODO Add sanity checks for existing values 
		// Add item to the map
		seqIdToValue.put(seqId, value);
		
		// Consume consecutive items from the map
		Iterator<Entry<S, T>> it = seqIdToValue.entrySet().iterator();
		while(it.hasNext()) {
			Entry<S, T> e = it.next();
			S s = e.getKey();
			T v = e.getValue();
			
			int d = Objects.compare(expectedSeqId, s, Comparator.naturalOrder());
			if(d == 0) {
				it.remove();
				delegate.accept(v);				
				expectedSeqId = getNextSeqId.apply(expectedSeqId);
			} else if(d < 0) {
				// This should not happen
				
				// Skip values with a lower id
				it.remove();
			} else { // if d > 0
				// Wait for the next sequence id
				break;
			}			
		}
	}	


	public static <T> Consumer<T> forLong(long initiallyExpectedId, Function<? super T, ? extends Long> getSeqId, Consumer<? super T> delegate) {
		return new ConsumerSorting<T, Long>(initiallyExpectedId, getSeqId, id -> Long.valueOf(id.longValue() + 1l), delegate);
	}
}


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

    public InputStreamManagerImpl() {
        this.readProtocol = new ChunkedProtocolReaderSimple();
        this.controlProtocol = new ChunkedProtocolControlSimple();    	
    }

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
                    	// Here we create a consumer instance that orders bytebuffers by their sequence id
                    	protected Consumer<ByteBuffer> appender = ConsumerSorting.forLong(
                    			1l, b -> ((Number)readProtocol.getChunkId(b)).longValue(),
                    			t -> {
									try {
			                            ByteBuffer payload = readProtocol.getPayload(buffer);
			                            ByteBuffer copy = ByteBuffer.allocate(payload.remaining());
			                            copy.put(payload);
			                            copy.rewind();                                

			                            super.appendDataToQueue(copy);
									} catch (InterruptedException e) {
										throw new RuntimeException(e);
									}
								});
                    	
                        @Override
                        protected void appendDataToQueue(ByteBuffer buffer) throws InterruptedException {
                            ByteBuffer copy = ByteBuffer.allocate(buffer.remaining());
                            copy.put(buffer.duplicate());
                            copy.rewind();                                

                            
                            // Make sure to pass only copies to the listeners so that
                            // (a) an overwrite of the original buffer does not cause undefined behavior
                            // (b) changes by the listeners do not cause undefined behavior
                            
                            try {
                                boolean isLastChunk = readProtocol.isLastChunk(buffer);
                                if(isLastChunk) {
                                    setLastBatchSeen();
                                }
                                                    
//                                in.appendDataToQueue(copy);
                            	appender.accept(copy);
                            } catch(Exception e) {
                                throw new RuntimeException(e);
                            }

                        	
                        	
                        }
                    	
                    	@Override
                        public void close() throws IOException {
                            // If the byte channel is closed before completed, send a control message
                            // to hint any sender to stop sending the stream
                            if(!isDone()) {
                                ByteBuffer readingAbortedMessage = controlProtocol.write(ByteBuffer.allocate(16), streamId, StreamControl.READING_ABORTED.getCode());
                                if(controlChannel != null) {
                                	controlChannel.accept(readingAbortedMessage);
                                }
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
                        executorService.execute(() -> {
                            try {
                                subscriber.accept(tmpIn);
                            } catch(Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

                        //});
                    }
                }
            }

            if(in != null) {
            	try {
					in.appendDataToQueue(buffer);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
//                ByteBuffer payload = readProtocol.getPayload(buffer);
//
//                
//                // Make sure to pass only copies to the listeners so that
//                // (a) an overwrite of the original buffer does not cause undefined behavior
//                // (b) changes by the listeners do not cause undefined behavior
//                ByteBuffer copy = ByteBuffer.allocate(payload.remaining());
//                copy.put(payload);
//                copy.rewind();                                
//                
//                try {
//                    boolean isLastChunk = readProtocol.isLastChunk(buffer);
//                    if(isLastChunk) {
//                        in.setLastBatchSeen();
//                    }
//                                        
//                    in.appendDataToQueue(copy);
//                } catch(InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
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
