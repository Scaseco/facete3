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
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

class ConsumerSorting<T, S extends Comparable<S>>
	implements Subscriber<T>
{
	protected Subscriber<? super T> delegate; //Consumer<? super T> delegate;
	
	protected Function<? super T, ? extends S> getSeqId;
	protected Function<? super S, ? extends S> incrementSeqId;

	//protected DiscreteDomain<S> discreteDomain;

	protected S expectedSeqId;
	protected boolean isComplete = false;
	
	protected NavigableMap<S, T> seqIdToValue = new TreeMap<>();

	
	public ConsumerSorting(
			S expectedSeqId,
			Function<? super T, ? extends S> extractSeqId,
			Function<? super S, ? extends S> incrementSeqId,
			Subscriber<? super T> delegate) {
		super();
		this.getSeqId = extractSeqId;
		this.incrementSeqId = incrementSeqId;
		this.expectedSeqId = expectedSeqId;
		this.delegate = delegate;		
	}

	public synchronized void onError(Throwable throwable) {
		delegate.onError(throwable);
	
		//throw new RuntimeException(throwable);
	}
		
	public synchronized void onComplete() {
		isComplete = true;
		
		// If there are no more entries in the map, complete the delegate immediately
		if(seqIdToValue.isEmpty()) {
			delegate.onComplete();
		}
		
		// otherwise, the onNext method has to handle completion
	}

	public synchronized void onNext(T value) {
		S seqId = getSeqId.apply(value);

		// If complete, the seqId must not be higher than the latest seen one
		if(isComplete) {
			if(seqIdToValue.isEmpty()) {
				onError(new RuntimeException("Sanity check failed: Call to onNext encountered after completion."));
			}
			
			S highestSeqId = seqIdToValue.descendingKeySet().first();
			
			if(Objects.compare(seqId, highestSeqId, Comparator.naturalOrder()) > 0) {
				onError(new RuntimeException("Sequence was marked as complete with id " + highestSeqId + " but a higher id was encountered " + seqId));
			}
		}

		boolean checkForExistingKeys = true;
		if(checkForExistingKeys) {
			if(seqIdToValue.containsKey(seqId)) {
				onError(new RuntimeException("Already seen an item with id " + seqId));
			}
		}

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
				delegate.onNext(v);				
				expectedSeqId = incrementSeqId.apply(expectedSeqId);
				//System.out.println("expecting seq id " + expectedSeqId);
			} else if(d < 0) {
				// Skip values with a lower id
				// TODO Add a flag to emit onError event
				System.out.println("Should not happen: received id " + s + " which is lower than the expected id " + expectedSeqId);
				it.remove();
			} else { // if d > 0
				// Wait for the next sequence id
				System.out.println("Received id " + s + " while waiting for expected id " + expectedSeqId);
				break;
			}			
		}
		
		// If the completion mark was set and all items have been emitted, we are done
		if(isComplete && seqIdToValue.isEmpty()) {
			delegate.onComplete();
		}
	}	

	@Override
	public synchronized void onSubscribe(Subscription s) {
		// TODO Auto-generated method stub		
	}


	public static <T> Subscriber<T> forLong(long initiallyExpectedId, Function<? super T, ? extends Long> getSeqId, Subscriber<? super T> delegate) {
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

    protected ChunkedProtocolReader<ByteBuffer> readProtocol;
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
    public synchronized boolean handleIncomingData(ByteBuffer buffer) {
    	buffer = buffer.duplicate();
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
                    stream.onError(new RuntimeException("reading aborted"));
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
                	AtomicLong count = new AtomicLong();
                    ReadableByteChannelSimple tmp = new ReadableByteChannelSimple() {
                    	// Here we create a consumer instance that orders bytebuffers by their sequence id
                    	protected Consumer<ByteBuffer> appender = ConsumerSorting.forLong(
                    			1l, b -> ((Number)readProtocol.getChunkId(b)).longValue(),
                    			t -> {
									try {
		                                boolean isLastChunk = readProtocol.isLastChunk(t);

		                                ByteBuffer payload = readProtocol.getPayload(t);
			                            ByteBuffer copy = ByteBuffer.allocate(payload.remaining());
			                            copy.put(payload.duplicate());
			                            copy.rewind();                                

			                            System.out.println("[STREAM] Appended data " + count.incrementAndGet());
			                            super.onNext(copy);
		                                if(isLastChunk) {
		                                    onComplete();
		                                }

									} catch (InterruptedException e) {
										throw new RuntimeException(e);
									}
								});
                    	
                        @Override
                        protected void onNext(ByteBuffer buffer) throws InterruptedException {
                            ByteBuffer copy = ByteBuffer.allocate(buffer.remaining());
                            copy.put(buffer.duplicate());
                            copy.rewind();                                

                            
                            // Make sure to pass only copies to the listeners so that
                            // (a) an overwrite of the original buffer does not cause undefined behavior
                            // (b) changes by the listeners do not cause undefined behavior
                            
                            try {                                                    
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
                            if(!isComplete()) {
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
					in.onNext(buffer);
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
