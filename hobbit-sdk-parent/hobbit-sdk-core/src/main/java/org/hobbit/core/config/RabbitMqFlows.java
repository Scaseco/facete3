package org.hobbit.core.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownListener;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.subscribers.DefaultSubscriber;

public class RabbitMqFlows {
	
	private static final Logger logger = LoggerFactory.getLogger(RabbitMqFlows.class);
	
    /**
     * Maximum number of retries that are executed to connect to RabbitMQ.
     */
    public static final int NUMBER_OF_RETRIES_TO_CONNECT_TO_RABBIT_MQ = 5;
    /**
     * Time, the system waits before retrying to connect to RabbitMQ. Note that
     * this time will be multiplied with the number of already failed tries.
     */
    public static final long START_WAITING_TIME_BEFORE_RETRY = 5000;

    

    protected static Connection createConnection(ConnectionFactory connectionFactory) throws Exception {
    	
    	Connection connection = null;
        for (int i = 0; (connection == null) && (i <= NUMBER_OF_RETRIES_TO_CONNECT_TO_RABBIT_MQ); ++i) {
            try {
                connection = connectionFactory.newConnection();
            } catch (Exception e) {
                if (i < NUMBER_OF_RETRIES_TO_CONNECT_TO_RABBIT_MQ) {
                    long waitingTime = START_WAITING_TIME_BEFORE_RETRY * (i + 1);
                    logger.warn("Couldn't connect to RabbitMQ with try #" + i + ". Next try in " + waitingTime + "ms.",
                            e);
                    try {
                        Thread.sleep(waitingTime);
                    } catch (Exception e2) {
                    	logger.warn("Interrupted while waiting before retrying to connect to RabbitMQ.", e2);
                    }
                }
            }
        }
        if (connection == null) {
            String msg = "Couldn't connect to RabbitMQ after " + NUMBER_OF_RETRIES_TO_CONNECT_TO_RABBIT_MQ
                    + " retries.";
            logger.error(msg);
            throw new Exception(msg);
        }
        return connection;
    }
    

    /**
     * Invoke queueDeclare() on short-lived channel in order to obtain an allocated channel name
     * As the channel is closed, the channel may get auto-deleted immediately again.
     * 
     * @param connection
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    public static String allocateDefaultQueueName(Connection connection) throws IOException, TimeoutException {
    	Channel channel = connection.createChannel();
    	String queueName;
    	try {
	    	queueName = channel.queueDeclare().getQueue();
	    	//System.out.println("Allocated queue name " + queueName);
    	} finally {
   	    	channel.close();    		
    	}
    	return queueName;
    }
    
    
    
    public static Function<Channel, String> createDefaultQueueDeclare(String queueName, String exchangeName) throws IOException, TimeoutException {
    	
    	Function<Channel, String> queueDeclare = (ch) -> {
    		try {
	    		ch.queueDeclare(queueName, false, true, true, null);
	            ch.exchangeDeclare(exchangeName, "fanout", false, true, null);
	            ch.queueBind(queueName, exchangeName, "");
    		} catch(Exception e) {
    			throw new RuntimeException(e);
    		}
            return queueName;
    	};
    	
    	return queueDeclare;
    }

    
    
    /**
     * Creates a flowable that for each subscriber
     * allocates a new channel and queue that listens on the given exchange.
     * 
     * 
     * @param connection
     * @param exchangeName
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    public static Flowable<ByteBuffer> createFanoutReceiver(Connection connection, String exchangeName) throws IOException, TimeoutException {
    	Flowable<ByteBuffer> flowable = createReplyableFanoutReceiver(connection, exchangeName).map(SimpleReplyableMessage::getValue);    	
    	return flowable;
    }
    

    /**
     * Creates a flowable that for each subscriber
     * allocates a new channel and queue that listens on the given exchange.
     * Emitted objects hold (a) the message value and (b) offers
     * a method to reply (respond) to the sender, thus facilitating a simple RPC pattern.
     * 
     * The reply method opens short-lived channels for this purpose.
     * 
     * @param connection
     * @param exchangeName
     * @return
     * @throws IOException
     * @throws TimeoutException
     */
    public static Flowable<SimpleReplyableMessage<ByteBuffer>> createReplyableFanoutReceiver(Connection connection, String exchangeName) throws IOException, TimeoutException {


//        String queueName = channel.queueDeclare().getQueue();
//        channel.exchangeDeclare(exchangeName, "fanout", false, true, null);
//        channel.queueBind(queueName, exchangeName, "");
//        
//        channel.close();

    	String queueName = allocateDefaultQueueName(connection);
    	Function<Channel, String> queueDeclare = createDefaultQueueDeclare(queueName, exchangeName);
    	
    	Flowable<SimpleReplyableMessage<ByteBuffer>> flowable = createFlowableForQueue(connection, queueDeclare);
    	
    	return flowable;
    }
    

    public static Flowable<SimpleReplyableMessage<ByteBuffer>> createReplyableFanoutReceiver(Channel channel, String exchangeName) throws IOException, TimeoutException {
        String queueName = channel.queueDeclare().getQueue();
        channel.exchangeDeclare(exchangeName, "fanout", false, true, null);
        channel.queueBind(queueName, exchangeName, "");

        Flowable<SimpleReplyableMessage<ByteBuffer>> flowable = createFlowableForChannel(channel, queueName, channel);
  	
        return flowable;
    }
  

    public static Flowable<ByteBuffer> createFanoutReceiver(Channel channel, String exchangeName) throws IOException, TimeoutException {
        Flowable<ByteBuffer> result = createReplyableFanoutReceiver(channel, exchangeName).map(SimpleReplyableMessage::getValue); 
  	
        return result;
    }


	public static Subscriber<ByteBuffer> createFanoutSender(Connection connection, String exchangeName, Function<ByteBuffer, ByteBuffer> transformer) throws IOException {
    	Channel channel = connection.createChannel();

    	BasicProperties properties = new BasicProperties.Builder()
    			.deliveryMode(2)
    			.build();

    	Consumer<ByteBuffer> coreConsumer = wrapPublishAsConsumer(channel, exchangeName, "", properties);
    	Consumer<ByteBuffer> consumer = transformer == null ? coreConsumer : (buffer) -> {
    		ByteBuffer msg = transformer.apply(buffer);
    		coreConsumer.accept(msg);
    	};

    	Subscriber<ByteBuffer> subscriber = wrapPublishAsSubscriber(consumer, () -> {
    		channel.close();
    		return 0;
    	});

    	return subscriber;
    }

	public static Subscriber<ByteBuffer> createFanoutSender(Channel channel, String exchangeName, Function<ByteBuffer, ByteBuffer> transformer) throws IOException {
    	BasicProperties properties = new BasicProperties.Builder()
    			.deliveryMode(2)
    			.build();

    	Consumer<ByteBuffer> coreConsumer = wrapPublishAsConsumer(channel, exchangeName, "", properties);
    	Consumer<ByteBuffer> consumer = transformer == null ? coreConsumer : (buffer) -> {
    		ByteBuffer msg = transformer.apply(buffer);
    		coreConsumer.accept(msg);
    	};

    	Subscriber<ByteBuffer> subscriber = wrapPublishAsSubscriber(consumer, () -> 0);

    	return subscriber;
    }


    public static <T> Subscriber<T> wrapPublishAsSubscriber(Consumer<T> consumer, Callable<?> closeAction) {
    	
		Subscriber<T> result = new DefaultSubscriber<T>() {
			@Override
			public void onNext(T t) {
				consumer.accept(t);
			}
			
			@Override
			public void onComplete() {
				Optional.ofNullable(closeAction).ifPresent(t -> {
					try {
						t.call();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				});
			}

			@Override
			public void onError(Throwable t) {
				throw new RuntimeException(t);
			}
		};
		
		return result;
    	
    }
    
    public static Consumer<ByteBuffer> createReplyToConsumer(Connection conn, String replyTo) {

		// Set up a lambda that for the sake of sending a response creates a one-shot
		// channel to send a message to the recipient specified in the replyTo field
		// This approach should be thread safe in accordance with the rabbitmq spec
		// i.e. sending the response can be done
		// by a different thread than the one that handled the request
		
		// The down-side is, that although multiple messages can be sent as repsonses,
		// a new connection is created for each of them
		// However, as the main use case here is to facilitate a simple RPC pattern
		// this should be acceptable
		Consumer<ByteBuffer> result = replyTo == null ? null : (byteBuffer) -> {
			Channel tmpChannel = null;
			try {
				tmpChannel = conn.createChannel();
		    	BasicProperties props = new BasicProperties.Builder()
		    			.deliveryMode(2)
		    			.build();

				tmpChannel.basicPublish(replyTo, "", props, byteBuffer.array());
				System.out.println("Publishing reply to " + replyTo);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				try {
					if(tmpChannel != null) {
						//tmpChannel.waitForConfirms();
						System.out.println("Closing reply channel " + tmpChannel + " isOpen=" + tmpChannel.isOpen());
						tmpChannel.close();
					}
				} catch (IOException | TimeoutException e) {
					throw new RuntimeException(e);
				}
			}
		};
		
		return result;
    }
    
    
    /**
     * Create a consumer that publishes to a queue on a specific channel
     * 
     * @param channel
     * @param replyTo
     * @return
     */
    public static Consumer<ByteBuffer> createReplyToConsumer(Channel channel, String replyTo) {

		// Set up a lambda that for the sake of sending a response creates a one-shot
		// channel to send a message to the recipient specified in the replyTo field
		// This approach should be thread safe in accordance with the rabbitmq spec
		// i.e. sending the response can be done
		// by a different thread than the one that handled the request
		
		// The down-side is, that although multiple messages can be sent as repsonses,
		// a new connection is created for each of them
		// However, as the main use case here is to facilitate a simple RPC pattern
		// this should be acceptable
		Consumer<ByteBuffer> result = replyTo == null ? null : (byteBuffer) -> {
			try {
		    	BasicProperties props = new BasicProperties.Builder()
		    			.deliveryMode(2)
		    			.build();

				channel.basicPublish(replyTo, "", props, byteBuffer.array());
				System.out.println("Publishing reply to " + replyTo);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};
		
		return result;
    }
    
    /**
     * Creates a flowable which re-publishes all messages received from a queue on a given channel,
     * and supports sending replies via a given response channel.
     * 
     * No life cycle management is performed on the channel.
     * Should the underlying channel close, onComplete is called.
     * 
     * 
     * 
     * @param channel
     * @return
     * @throws IOException 
     */
    public static Flowable<SimpleReplyableMessage<ByteBuffer>> createFlowableForChannel(Channel channel, String queueName, Channel responseChannel) throws IOException {
    	Flowable<SimpleReplyableMessage<ByteBuffer>> result = Flowable.create(new FlowableOnSubscribe<SimpleReplyableMessage<ByteBuffer>>() {
	        @Override
	        public void subscribe(final FlowableEmitter<SimpleReplyableMessage<ByteBuffer>> emitter) throws Exception {

	        	ShutdownListener shutdownListener = (throwable) -> {
	        		if(throwable != null) {
	        			emitter.onError(throwable);
	        		}
	        		emitter.onComplete();
	        		//channel.removeShutdownListener(shutdownListener);
	        	};
	        	
	        	
//	        	System.out.println("Created channel " + channel + " for queue " + queueName + " connIsOpen=" + connection.isOpen());
	        	
	        	channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
	        		@Override
	        		public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
	        				throws IOException {
	    				String replyTo = properties.getReplyTo();
	    				
	    				Consumer<ByteBuffer> reply = createReplyToConsumer(responseChannel, replyTo);
	    				
	    				
	    				//System.out.println("Received message from queue " + queueName);
	    		    	ByteBuffer buffer = ByteBuffer.wrap(body);
	    		    	
	    		    	SimpleReplyableMessage<ByteBuffer> msg = new SimpleReplyableMessageImpl<ByteBuffer>(buffer, reply);
	    		    	
	    		    	//result.onSubscribe();
	    		    	
	    		    	//result.onNext(buffer);
	    		    	emitter.onNext(msg);
	    		    }	        		
	        	});
	        	
	        	
	        	//emitter.setCancellable(channel::close);

	        	emitter.setCancellable(() -> {
	        		channel.removeShutdownListener(shutdownListener);
	        	});
	        }
	    }, BackpressureStrategy.BUFFER);   
    	
    	
    	
    	
    	
    	
//    	PublishProcessor<SimpleReplyableMessage<ByteBuffer>> result = PublishProcessor.create();
//
//    	channel.addShutdownListener((throwable) -> {
//    		if(throwable != null) {
//    			result.onError(throwable);
//    		}
//    		result.onComplete();
//    		
////    		channel.removeShutdownListener(listener);
//    	});
//
//    	
//    	channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
//    		@Override
//    		public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
//    				throws IOException {
//				String replyTo = properties.getReplyTo();
//				
//				Consumer<ByteBuffer> reply = createReplyToConsumer(responseChannel, replyTo);
//				
//				
//				//System.out.println("Received message from queue " + queueName);
//		    	ByteBuffer buffer = ByteBuffer.wrap(body);
//		    	
//		    	SimpleReplyableMessage<ByteBuffer> msg = new SimpleReplyableMessageImpl<ByteBuffer>(buffer, reply);
//		    	
//		    	//result.onSubscribe();
//		    	
//		    	//result.onNext(buffer);
//		    	result.onNext(msg);
//    		}
//    	});
    	
    	return result;
    }
    
    /**
     * Creates a flowable which on every subscription creates a new channel to the declared queue
     * 
     * 
     * @param connection
     * @param queueDeclare
     * @return
     * @throws IOException
     */
    public static Flowable<SimpleReplyableMessage<ByteBuffer>> createFlowableForQueue(Connection connection, Function<Channel, String> queueDeclare) throws IOException {
    	Flowable<SimpleReplyableMessage<ByteBuffer>> result = Flowable.create(new FlowableOnSubscribe<SimpleReplyableMessage<ByteBuffer>>() {
	        @Override
	        public void subscribe(final FlowableEmitter<SimpleReplyableMessage<ByteBuffer>> emitter) throws Exception {

	        	Channel channel = connection.createChannel();

	        	String queueName = queueDeclare.apply(channel);
	        	
	        	System.out.println("Created channel " + channel + " for queue " + queueName + " connIsOpen=" + connection.isOpen());
	        	
	        	channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
	        		@Override
	        		public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
	        				throws IOException {
	    				String replyTo = properties.getReplyTo();
	    				
	    				Consumer<ByteBuffer> reply = createReplyToConsumer(connection, replyTo);
	    				
	    				
	    				//System.out.println("Received message from queue " + queueName);
	    		    	ByteBuffer buffer = ByteBuffer.wrap(body);
	    		    	
	    		    	SimpleReplyableMessage<ByteBuffer> msg = new SimpleReplyableMessageImpl<ByteBuffer>(buffer, reply);
	    		    	
	    		    	//result.onSubscribe();
	    		    	
	    		    	//result.onNext(buffer);
	    		    	emitter.onNext(msg);
	    		    }	        		
	        	});
	        	
	        	
	        	//emitter.setCancellable(channel::close);

	        	emitter.setCancellable(() -> {
	        		System.out.println("Closing channel " + channel + " isOpen=" + channel.isOpen() + " connIsOpen=" + connection.isOpen());
	        		
	        		channel.close();
	        	});
	        }
	    }, BackpressureStrategy.BUFFER);    	
    	
    	//PublishProcessor<SimpleReplyableMessage<ByteBuffer>> result = PublishProcessor.create();
//    	
//    	Connection conn = channel.getConnection();
//    	
//    	channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
//			@Override
//			public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
//					throws IOException {
//
//				String replyTo = properties.getReplyTo();
//				
//				Consumer<ByteBuffer> reply = createReplyToConsumer(conn, replyTo);
//				
//				
//				//System.out.println("Received message from queue " + queueName);
//		    	ByteBuffer buffer = ByteBuffer.wrap(body);
//		    	
//		    	SimpleReplyableMessage<ByteBuffer> msg = new SimpleReplyableMessageImpl<ByteBuffer>(buffer, reply);
//		    	
//		    	//result.onSubscribe();
//		    	
//		    	//result.onNext(buffer);
//		    	result.onNext(msg);
//			}
//		});

//        Flowable<ByteBuffer> result = Flowable.create(new FlowableOnSubscribe<ByteBuffer>() {
//	        @Override
//	        public void subscribe(final FlowableEmitter<ByteBuffer> emitter) throws Exception {
//	        	channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
//	        		@Override
//	        		public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
//	        				throws IOException {
//	        			System.out.println("Received message from queue " + queueName);
//	        	    	ByteBuffer buffer = ByteBuffer.wrap(body);
//	        	    	emitter.onNext(buffer);
//	        		}
//	        	});
//	        }
//	    }, BackpressureStrategy.BUFFER);
//	

    	//result = result.doOnSubscribe(onSubscribe -> onSubscribe.)
    	return result;
    }
        
    public static Consumer<ByteBuffer> wrapPublishAsConsumer(Channel channel, String exchangeName, String routingKey, BasicProperties properties) {
    	Consumer<ByteBuffer> result = (buffer) -> {
    		try {
    			System.out.println("Publishing on channel " + channel + " isOpen=" + channel.isOpen());
    	    	channel.basicPublish(exchangeName, routingKey, properties, buffer.array());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
    	};
    	
    	return result;
    }

    /**
     * Creates both a subscriber and a flowable.
     * Every sent message has the replyTo field set, such that any replys will be made available via the flowable. 
     * 
     * @param connection
     * @param exchangeName
     * @param transformer
     * @return
     * @throws IOException
     * @throws TimeoutException 
     */
    public static Entry<Subscriber<ByteBuffer>, Flowable<ByteBuffer>> createReplyableFanoutSenderCore(Connection connection, String exchangeName, Function<ByteBuffer, ByteBuffer> transformer) throws IOException, TimeoutException {
    	Channel channel = connection.createChannel();

//    	Channel responseChannel = connection.createChannel();
//    	String responseQueueName = responseChannel.queueDeclare().getQueue();
    	
    	String responseQueueName = allocateDefaultQueueName(connection);
    	Function<Channel, String> responseQueueDeclare = createDefaultQueueDeclare(responseQueueName, exchangeName);
   	
    	
    	BasicProperties properties = new BasicProperties.Builder()
    			.deliveryMode(2)
    			.replyTo(responseQueueName)
    			.build();

    	Consumer<ByteBuffer> coreConsumer = wrapPublishAsConsumer(channel, exchangeName, "", properties);
    	Consumer<ByteBuffer> consumer = transformer == null ? coreConsumer : (buffer) -> {
    		ByteBuffer msg = transformer.apply(buffer);
    		coreConsumer.accept(msg);
    	};

    	Subscriber<ByteBuffer> subscriber = wrapPublishAsSubscriber(consumer, () -> {
    		channel.close();
    		return 0;
    	});

    	Flowable<ByteBuffer> flowable = createFlowableForQueue(connection, responseQueueDeclare).map(SimpleReplyableMessage::getValue);
    	Entry<Subscriber<ByteBuffer>, Flowable<ByteBuffer>> result = new SimpleEntry<>(subscriber, flowable);
    	
    	return result;
    }

    
    public static Entry<Subscriber<ByteBuffer>, Flowable<ByteBuffer>> createReplyableFanoutSenderCore(Channel channel, String exchangeName, Function<ByteBuffer, ByteBuffer> transformer) throws IOException, TimeoutException {
//    	Channel channel = connection.createChannel();

//    	Channel responseChannel = connection.createChannel();
//    	String responseQueueName = responseChannel.queueDeclare().getQueue();
    	
    	//String responseQueueName = allocateDefaultQueueName(connection);
    	//Function<Channel, String> responseQueueDeclare = createDefaultQueueDeclare(responseQueueName, exchangeName);
   	
    	
		String responseQueueName = channel.queueDeclare().getQueue();
        channel.exchangeDeclare(exchangeName, "fanout", false, true, null);
        channel.queueBind(responseQueueName, exchangeName, "");

    	
    	BasicProperties properties = new BasicProperties.Builder()
    			.deliveryMode(2)
    			.replyTo(responseQueueName)
    			.build();

    	Consumer<ByteBuffer> coreConsumer = wrapPublishAsConsumer(channel, exchangeName, "", properties);
    	Consumer<ByteBuffer> consumer = transformer == null ? coreConsumer : (buffer) -> {
    		ByteBuffer msg = transformer.apply(buffer);
    		coreConsumer.accept(msg);
    	};

    	Subscriber<ByteBuffer> subscriber = wrapPublishAsSubscriber(consumer, () -> {
    		channel.close();
    		return 0;
    	});

    	Flowable<ByteBuffer> flowable = createFlowableForChannel(channel, responseQueueName, channel).map(SimpleReplyableMessage::getValue);
    	Entry<Subscriber<ByteBuffer>, Flowable<ByteBuffer>> result = new SimpleEntry<>(subscriber, flowable);
    	
    	return result;
    }
    public static Function<ByteBuffer, CompletableFuture<ByteBuffer>> createReplyableFanoutSender(Connection connection, String exchangeName, Function<ByteBuffer, ByteBuffer> transformer) throws IOException, TimeoutException {
    	Entry<Subscriber<ByteBuffer>, Flowable<ByteBuffer>> entry = createReplyableFanoutSenderCore(connection, exchangeName, transformer);
    	Function<ByteBuffer, CompletableFuture<ByteBuffer>> result = wrapAsFunction(entry);
    	
    	return result;
    }

    public static Function<ByteBuffer, CompletableFuture<ByteBuffer>> createReplyableFanoutSender(Channel channel, String exchangeName, Function<ByteBuffer, ByteBuffer> transformer) throws IOException, TimeoutException {
    	Entry<Subscriber<ByteBuffer>, Flowable<ByteBuffer>> entry = createReplyableFanoutSenderCore(channel, exchangeName, transformer);
    	Function<ByteBuffer, CompletableFuture<ByteBuffer>> result = wrapAsFunction(entry);
    	
    	return result;
    }
    
    public static <T> Function<T, CompletableFuture<T>> wrapAsFunction(Entry<Subscriber<T>, Flowable<T>> entry) {
    	Subscriber<T> subscriber = entry.getKey();
    	Flowable<T> flowable = entry.getValue();
    	Function<T, CompletableFuture<T>> result = wrapAsFunction(subscriber, flowable);

    	return result;
    }

    public static <I, O> Function<I, CompletableFuture<O>> wrapAsFunction(Subscriber<I> subscriber, Flowable<O> flowable) {
    	Function<I, CompletableFuture<O>> result = t -> {
    		
    		// Send request
    		subscriber.onNext(t);

    		// Await response
    		System.out.println("Awaiting response");
    		O r = flowable.blockingFirst();
    		System.out.println("Got response: " + r);
    		
    		
    		
    		// TODO properly create the completable future
    		//O r = flowable.timeout(60, TimeUnit.SECONDS).blockingFirst();
    		//O r = flowable.blockingNext();
    		
//    		CompletableFuture<O> tmp = new CompletableFuture<>();
//    		flowable.doOnNext(r -> {
//    			System.out.println("Got response object; resolving future");
//    			tmp.complete(r);
//    		});
    		
    		CompletableFuture<O> tmp = CompletableFuture.completedFuture(r);
    		return tmp;
    	};
    	
    	return result;
    }
    
    public static Flowable<ByteBuffer> createDataReceiverCore(Connection connection, String queueName) throws IOException, TimeoutException {

//    	Channel channel = connection.createChannel();
//    	channel.queueDeclare(queueName, false, false, true, null);
//    	channel.close();

    	Function<Channel, String> queueDeclare = (channel) -> {
        	try {
				channel.queueDeclare(queueName, false, false, true, null);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
        	return queueName;
    	};
    	
    	
    	Flowable<ByteBuffer> flowable = RabbitMqFlows.createFlowableForQueue(connection, queueDeclare).map(SimpleReplyableMessage::getValue);

    	return flowable;
    }

    public static Subscriber<ByteBuffer> createDataSenderCore(Connection connection, String queueName) throws IOException {

    	Channel channel = connection.createChannel();
    	channel.queueDeclare(queueName, false, false, true, null);

    	BasicProperties properties = new BasicProperties.Builder()
    			.deliveryMode(2)
    			.build();

    	Consumer<ByteBuffer> coreConsumer = RabbitMqFlows.wrapPublishAsConsumer(channel, "", queueName, properties);
    	Subscriber<ByteBuffer> subscriber = RabbitMqFlows.wrapPublishAsSubscriber(coreConsumer, () -> {
    		channel.close();
    		return 0;
    	});
    	
    	return subscriber;
    }

}

