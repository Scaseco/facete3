package org.hobbit.core.config;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.hobbit.core.Constants;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import io.reactivex.Flowable;

public class HobbitConfigChannelsPlatform {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HobbitConfigChannelsPlatform.class);
		
	@Inject
	protected Environment env;
		
	protected String hobbitSessionId;
    

	@PostConstruct
    public void postConstruct() throws Exception {  	
    	hobbitSessionId = env.getProperty(Constants.HOBBIT_SESSION_ID_KEY, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS);


//        incomingDataConnection = createConnection();
//        outgoingDataConnection = createConnection();
	}

    public String generateSessionQueueName(String queueName) {
        return queueName + "." + hobbitSessionId;
    }

	/**
	 * Helper function where context specific connection configuration could be made
	 * 
	 * @param connectionFactory
	 * @return
	 * @throws Exception
	 */
	public Connection createConnection(ConnectionFactory connectionFactory) throws Exception {
		return RabbitMqFlows.createConnection(connectionFactory);
	}

	public Subscriber<ByteBuffer> createDataSender(Connection connection, String baseQueueName) throws IOException {
		String queueName = generateSessionQueueName(baseQueueName);
		return RabbitMqFlows.createDataSenderCore(connection, queueName);
	}

    public Flowable<ByteBuffer> createDataReceiver(Connection connection, String baseQueueName) throws IOException, TimeoutException {
    	String queueName = generateSessionQueueName(baseQueueName);
    	return RabbitMqFlows.createDataReceiverCore(connection, queueName);
    }

    
//    public ChannelWrapper<ByteBuffer> createCommandChannelWrapper(Connection connection, String exchangeName) throws IOException {
//    	ChannelWrapper<ByteBuffer> result = new ChannelWrapper<>(subscriber, flowable);
//    	return result;
//    }
    
    
    /**
     * Data channel uses exchange="" and queueName=
     * 
     * @param channel
     * @param queueName
     * @return
     * @throws IOException
     */
//    public static ChannelWrapper<ByteBuffer> createDataChannelWrapper(Channel channel, String queueName) throws IOException {
//    	channel.queueDeclare(queueName, false, false, true, null);
//    	
//    	BasicProperties properties = new BasicProperties.Builder()
//    			.deliveryMode(2)
//    			.build();
//
//    	Consumer<ByteBuffer> coreConsumer = wrapPublishAsConsumer(channel, "", queueName, properties);
//    	Subscriber<ByteBuffer> subscriber = wrapPublishAsSubscriber(coreConsumer, () -> { channel.close(); return 0; });
//
//    	Flowable<ByteBuffer> flowable = wrapChannel(channel, queueName).map(SimpleReplyableMessage::getValue);
//    	
//    	ChannelWrapper<ByteBuffer> result = new ChannelWrapper<>(subscriber, flowable);
//    	
//    	return result;
//    }


    public static Entry<String, ByteBuffer> parseCommandBuffer(ByteBuffer buffer) {
    	buffer = buffer.duplicate();
    	
    	String sessionId;
    	try {
    		sessionId = RabbitMQUtils.readString(buffer);
    	} catch(Exception e) {
    		throw new RuntimeException("Buffer content: " + Arrays.toString(buffer.array()), e);
    	}
    	System.out.println("Read sessionId: "+ sessionId);
//        if (acceptedCmdHeaderIds.contains(sessionId)) {
    	
//            byte command = buffer.get();
//            byte remainingData[];
//            if (buffer.remaining() > 0) {
//                remainingData = new byte[buffer.remaining()];
//                buffer.get(remainingData);
//            } else {
//                remainingData = new byte[0];
//            }
            
            
            //receiveCommand(command, remainingData);
//        } else {
//        	result = null;
//        }
    	byte[] remainingData = new byte[buffer.remaining()];
    	buffer.get(remainingData);
    	
    	ByteBuffer buf = ByteBuffer.wrap(remainingData);

    	Entry<String, ByteBuffer> result = new SimpleEntry<>(sessionId, buf);    	
    	
        return result;
    }
    
    
    public static ByteBuffer createCmdMessage(ByteBuffer dataBuf, String sessionId) {
        byte sessionIdBytes[] = sessionId.getBytes(StandardCharsets.UTF_8);
        // + 4 because 4 bytes for the session ID length and 0 byte for the
        // command - the cmd byte is part of the payload now ~ Claus

        
        int dataLength = 4 + sessionIdBytes.length + dataBuf.remaining();
        ByteBuffer buffer = ByteBuffer.allocate(dataLength);
        buffer.putInt(sessionIdBytes.length);
        buffer.put(sessionIdBytes);
        buffer.put(dataBuf);
        buffer.rewind();
        
        //System.out.println("Sending command (" + buffer.remaining() + " bytes)");
        return buffer;
    }
    
    
	@Bean
	public Connection commandConnection(ConnectionFactory connectionFactory) throws Exception {
		return RabbitMqFlows.createConnection(connectionFactory);
	}


    
//    public static defaultQueueDeclare()

//    
//
//    public static Subscriber<ByteBuffer> createDataSenderCore(Connection connection, String queueName) throws IOException {
//    	String queueName = generateSessionQueueName(baseQueueName);
//
//    	Channel channel = connection.createChannel();
//    	channel.queueDeclare(queueName, false, false, true, null);
//
//    	BasicProperties properties = new BasicProperties.Builder()
//    			.deliveryMode(2)
//    			.build();
//
//    	Consumer<ByteBuffer> coreConsumer = RabbitMqFlows.wrapPublishAsConsumer(channel, "", queueName, properties);
//    	Subscriber<ByteBuffer> subscriber = RabbitMqFlows.wrapPublishAsSubscriber(coreConsumer, () -> {
//    		channel.close();
//    		return 0;
//    	});
//    	
//    	return subscriber;
//    }

    
    
//    public ChannelWrapper<ByteBuffer> createWrappedFanoutChannel(Connection connection, String baseQueueName) throws IOException {
//    	Channel channel = connection.createChannel();
//    	
//    	String queueName = generateSessionQueueName(baseQueueName);
//    	//Channel channel = createFanoutChannel(connection, queueName);
//    	ChannelWrapper<ByteBuffer> result = createDataChannelWrapper(channel, queueName);
//    	return result;    	
//    }

    /*
     * data connections
     */
    @Bean
    public Connection outgoingCommandConnection(ConnectionFactory connectionFactory) throws Exception {
    	return RabbitMqFlows.createConnection(connectionFactory);
    }

    @Bean
    public Connection incomingCommandConnection(ConnectionFactory connectionFactory) throws Exception {
    	return createConnection(connectionFactory);
    }
    
    @Bean
    public Connection outgoingDataConnection(ConnectionFactory connectionFactory) throws Exception {
    	return createConnection(connectionFactory);
    }

    @Bean
    public Connection incomingDataConnection(ConnectionFactory connectionFactory) throws Exception {
    	return createConnection(connectionFactory);
    }

    /*
     * command channel
     */
    
//    @Bean
//    public ChannelWrapper<ByteBuffer> rawCommandChannel(@Qualifier("commandConnection") Connection connection) throws IOException {
//    	return createCommandChannelWrapper(connection, Constants.HOBBIT_COMMAND_EXCHANGE_NAME);
//    }

    @Bean
    @Scope("prototype")
    public Subscriber<ByteBuffer> commandChannel(@Qualifier("outgoingCommandConnection") Connection connection) throws IOException {
    	return RabbitMqFlows.createFanoutSender(connection, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, (buffer) -> createCmdMessage(buffer, hobbitSessionId));
    }
        
 
    @Bean
    @Scope("prototype")
    public Flowable<ByteBuffer> commandPub(@Qualifier("incomingCommandConnection") Connection connection) throws IOException, TimeoutException {
    	return RabbitMqFlows.createFanoutReceiver(connection, Constants.HOBBIT_COMMAND_EXCHANGE_NAME).map(HobbitConfigChannelsPlatform::parseCommandBuffer).map(Entry::getValue);
    }


    /*
     * dg2tg
     */
    
//    @Bean
//    @Scope("prototype")
//    public ChannelWrapper<ByteBuffer> rawDg2tg(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
//    }
//
//    @Bean
//    @Scope("prototype")
//    public ChannelWrapper<ByteBuffer> rawDg2tgPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
//    }

    @Bean
    @Scope("prototype")
    public Subscriber<ByteBuffer> dg2tg(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
        return createDataSender(connection, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
    }

    @Bean
    @Scope("prototype")
    public Flowable<ByteBuffer> dg2tgPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException, TimeoutException {
        return createDataReceiver(connection, Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME);
    }


    /*
     * dg2sa
     */
    
//    @Bean
//    public ChannelWrapper<ByteBuffer> rawDg2sa(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
//    }
//
//    @Bean
//    @Scope("prototype")
//    public ChannelWrapper<ByteBuffer> rawDg2saPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
//    }
//
//    @Bean
//    @Scope("prototype")
//    public Subscriber<ByteBuffer> dg2sa(@Qualifier("rawDg2sa") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getSubscriber();
//    }
//
//    @Bean
//    @Scope("prototype")
//    public Flowable<ByteBuffer> dg2saPub(@Qualifier("rawDg2saPub") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getFlowable();
//    }

    @Bean
    @Scope("prototype")
    public Subscriber<ByteBuffer> dg2sa(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
        return createDataSender(connection, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
    }

    @Bean
    @Scope("prototype")
    public Flowable<ByteBuffer> dg2saPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException, TimeoutException {
        return createDataReceiver(connection, Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME);
    }

    
    /*
     * tg2sa
     */

    
//    @Bean
//    public ChannelWrapper<ByteBuffer> rawTg2sa(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
//    }
//
//    @Bean
//    public ChannelWrapper<ByteBuffer> rawTg2saPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
//    }
//
//    @Bean
//    public Subscriber<ByteBuffer> tg2sa(@Qualifier("rawTg2sa") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getSubscriber();
//    }
//    
//    @Bean
//    public Flowable<ByteBuffer> tg2saPub(@Qualifier("rawTg2saPub") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getFlowable();
//    }

    @Bean
    @Scope("prototype")
    public Subscriber<ByteBuffer> tg2sa(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
        return createDataSender(connection, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
    }

    @Bean
    @Scope("prototype")
    public Flowable<ByteBuffer> tg2saPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException, TimeoutException {
        return createDataReceiver(connection, Constants.TASK_GEN_2_SYSTEM_QUEUE_NAME);
    }

    
    /*
     * tg2es
     */
    
    
//    @Bean
//    public ChannelWrapper<ByteBuffer> rawTg2es(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
//    }
//
//    @Bean
//    public ChannelWrapper<ByteBuffer> rawTg2esPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
//    }
//    
//    @Bean
//    public Subscriber<ByteBuffer> tg2es(@Qualifier("rawTg2es") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getSubscriber();
//    }
//
//    @Bean
//    public Flowable<ByteBuffer> tg2esPub(@Qualifier("rawTg2esPub") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getFlowable();
//    }
    
    @Bean
    @Scope("prototype")
    public Subscriber<ByteBuffer> tg2es(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
        return createDataSender(connection, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
    }

    @Bean
    @Scope("prototype")
    public Flowable<ByteBuffer> tg2esPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException, TimeoutException {
        return createDataReceiver(connection, Constants.TASK_GEN_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
    }
    
    
    /*
     * sa2es
     */
        
//    @Bean
//    public ChannelWrapper<ByteBuffer> rawSa2es(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
//    }
//
//    @Bean
//    public ChannelWrapper<ByteBuffer> rawSa2esPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
//    }
//    
//    @Bean
//    public Subscriber<ByteBuffer> sa2es(@Qualifier("rawSa2es") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getSubscriber();
//    }
//
//    @Bean
//    public Flowable<ByteBuffer> sa2esPub(@Qualifier("rawSa2esPub") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getFlowable();
//    }
    
    @Bean
    @Scope("prototype")
    public Subscriber<ByteBuffer> sa2es(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
        return createDataSender(connection, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
    }

    @Bean
    @Scope("prototype")
    public Flowable<ByteBuffer> sa2esPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException, TimeoutException {
        return createDataReceiver(connection, Constants.SYSTEM_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
    }

    /*
     * taskAck (tg waits for ack, es sends it)
     * 
     */
    @Bean
    @Scope("prototype")
    public Subscriber<ByteBuffer> taskAck(@Qualifier("outgoingCommandConnection") Connection connection) throws IOException {
    	return RabbitMqFlows.createFanoutSender(connection, Constants.HOBBIT_ACK_EXCHANGE_NAME, null);
    }
        
 
    @Bean
    @Scope("prototype")
    public Flowable<ByteBuffer> taskAckPub(@Qualifier("incomingCommandConnection") Connection connection) throws IOException, TimeoutException {
    	return RabbitMqFlows.createFanoutReceiver(connection, Constants.HOBBIT_ACK_EXCHANGE_NAME);
    }

    
    /*
     * es2em
     */

//    @Bean
//    public ChannelWrapper<ByteBuffer> rawEs2em(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
//    }
//
//    @Bean
//    public ChannelWrapper<ByteBuffer> rawEs2emPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
//    }
//    
//    @Bean
//    public Subscriber<ByteBuffer> es2em(@Qualifier("rawEs2em") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getSubscriber();
//    }
//
//    @Bean
//    public Flowable<ByteBuffer> es2emPub(@Qualifier("rawEs2emPub") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getFlowable();
//    }
    
    @Bean
    @Scope("prototype")
    public Subscriber<ByteBuffer> es2em(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
        return createDataSender(connection, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
    }

    @Bean
    @Scope("prototype")
    public Flowable<ByteBuffer> es2emPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException, TimeoutException {
        return createDataReceiver(connection, Constants.EVAL_STORAGE_2_EVAL_MODULE_DEFAULT_QUEUE_NAME);
    }


    /*
     * em2es
     */

//    @Bean
//    public ChannelWrapper<ByteBuffer> rawEm2es(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
//    }
//
//    @Bean
//    public ChannelWrapper<ByteBuffer> rawEm2esPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException {
//    	return createWrappedFanoutChannel(connection, Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
//    }
//    
//    @Bean
//    public Subscriber<ByteBuffer> em2es(@Qualifier("rawEm2es") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getSubscriber();
//    }
//
//    @Bean
//    public Flowable<ByteBuffer> em2esPub(@Qualifier("rawEm2esPub") ChannelWrapper<ByteBuffer> channelWrapper) {
//        return channelWrapper.getFlowable();
//    }

    @Bean
    @Scope("prototype")
    public Subscriber<ByteBuffer> em2es(@Qualifier("outgoingDataConnection") Connection connection) throws IOException {
        return createDataSender(connection, Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
    }

    @Bean
    @Scope("prototype")
    public Flowable<ByteBuffer> em2esPub(@Qualifier("incomingDataConnection") Connection connection) throws IOException, TimeoutException {
        return createDataReceiver(connection, Constants.EVAL_MODULE_2_EVAL_STORAGE_DEFAULT_QUEUE_NAME);
    }

}



//
//public void foo() {
//	RabbitQueueFactory outgoingQueueFactory;
//	
//  DataSender sender2TaskGen = DataSenderImpl.builder().queue(outgoingQueueFactory,
//          generateSessionQueueName(Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME)).build();
//
//  DataSender sender2System = DataSenderImpl.builder().queue(outgoingQueueFactory,
//          generateSessionQueueName(Constants.DATA_GEN_2_SYSTEM_QUEUE_NAME)).build();
//
////  Flowable<ByteBuffer> flowable = Flowable.from
//  
//  
//  
//  DataReceiver dataGenReceiver = DataReceiverImpl.builder().dataHandler(new DataHandler() {
//      @Override
//      public void handleData(byte[] data) {
//          receiveGeneratedData(data);
//      }
//  }).maxParallelProcessedMsgs(maxParallelProcessedMsgs)
//          .queue(getFactoryForIncomingDataQueues(), generateSessionQueueName(Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME))
//          .build();
//
//}
//
//
//

//@Override
//public void init2() throws Exception {
////  super.init();
//	// The command header part is a filter on the flowable
//  addCommandHeaderId(getHobbitSessionId());
//
//  RabbitQueueFactory cmdQueueFactory = new RabbitQueueFactoryImpl(createConnection());
//  Channel cmdChannel = cmdQueueFactory.getConnection().createChannel();
//  String queueName = cmdChannel.queueDeclare().getQueue();
//  cmdChannel.exchangeDeclare(Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "fanout", false, true, null);
//  cmdChannel.queueBind(queueName, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "");
//
//  DefaultConsumer consumer = new DefaultConsumer(cmdChannel) {
//      @Override
//      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
//              byte[] body) throws IOException {
//          try {
//          	ByteBuffer buffer = ByteBuffer.wrap(body);
//          	
//          	
//              handleCmd(body, properties.getReplyTo());
//          } catch (Exception e) {
//              LOGGER.error("Exception while trying to handle incoming command.", e);
//          }
//      }
//  };
//  cmdChannel.basicConsume(queueName, true, consumer);
//
//  String containerName = env.getProperty(Constants.CONTAINER_NAME_KEY);
//  
//  if (containerName == null) {
//      LOGGER.info("Couldn't get the id of this Docker container. Won't be able to create containers.");
//  }
//}



//protected void sendToCmdQueue(byte command, byte data[], BasicProperties props) throws IOException {
//  byte sessionIdBytes[] = getHobbitSessionId().getBytes(Charsets.UTF_8);
//  // + 5 because 4 bytes for the session ID length and 1 byte for the
//  // command
//  int dataLength = sessionIdBytes.length + 5;
//  boolean attachData = (data != null) && (data.length > 0);
//  if (attachData) {
//      dataLength += data.length;
//  }
//  ByteBuffer buffer = ByteBuffer.allocate(dataLength);
//  buffer.putInt(sessionIdBytes.length);
//  buffer.put(sessionIdBytes);
//  buffer.put(command);
//  if (attachData) {
//      buffer.put(data);
//  }
//  cmdChannel.basicPublish(Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "", props, buffer.array());
//}




//public static Consumer<ByteBuffer> wrapDataChannelAsConsumer(Channel channel, String queueName) {
////	String responseQueueName = channel.queueDeclare().getQueue();
//	BasicProperties properties = new BasicProperties.Builder()
//			.deliveryMode(2)
////			.replyTo(responseQueueName)
//			.build();
//
//	Consumer<ByteBuffer> result = wrapPublishAsConsumer(channel, "", queueName, properties);
//	
//	return result;
//}


//public static Consumer<ByteBuffer> wrapCommandChannelAsConsumer(Channel channel) throws IOException {
//
//	String responseQueueName = channel.queueDeclare().getQueue();
//	BasicProperties properties = new BasicProperties.Builder()
//			.deliveryMode(2)
//			.replyTo(responseQueueName)
//			.build();
//
//	Consumer<ByteBuffer> result = wrapPublishAsConsumer(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "", properties);
//	
//  return result;
//}


//public static Flowable<ByteBuffer> wrapChannel(Channel channel, String queueName) {
//  Flowable<ByteBuffer> result = Flowable.create(new FlowableOnSubscribe<ByteBuffer>() {
//      @Override
//      public void subscribe(final FlowableEmitter<ByteBuffer> emitter) throws Exception {
//      	channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
//      		@Override
//      		public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
//      				throws IOException {
//      	    	ByteBuffer buffer = ByteBuffer.wrap(body);
//      	    	emitter.onNext(buffer);
//      		}
//      	});
//      }
//  }, BackpressureStrategy.BUFFER);    	
//
//	return result;
//}

  

//public static Channel createFanoutChannel(Connection connection, String exchangeName) throws IOException {
//  Channel result = connection.createChannel();
//  String queueName = result.queueDeclare().getQueue();
//  //result.queueDeclare()
//  result.exchangeDeclare(exchangeName, "fanout", false, true, null);
//  result.queueBind(queueName, exchangeName, "");
//	
//  return result;
//}

//public static Flowable<ByteBuffer> createFlowable(Connection connection, String queueName, String exchangeName) throws IOException {
//	Channel channel = createFanoutChannel(connection, exchangeName);
//	//String queueName = channel.queueDeclare().getQueue();
//
//	Flowable<ByteBuffer> result = wrapChannel(channel, queueName);
//	return result;
//}



//public static ChannelWrapper<ByteBuffer> createChannelWrapper(Channel channel, String exchangeName, String queueName) throws IOException {
//	String responseQueueName = channel.queueDeclare().getQueue();
//	BasicProperties props = new BasicProperties.Builder()
//			.deliveryMode(2)
//			.replyTo(responseQueueName)
//			.build();
//	
//	
//	Function<ByteBuffer, Integer> consumer = (buffer) -> {
//		try {
//			byte[] array = buffer.array();
//			channel.basicPublish(exchangeName, "", props, array);
//			return array.length;
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//	};
//
//	//createFlowable(queueFactory, exchangeName);
//			
//	//DefaultSubscriber()
//	//WritableByteChannelImpl.wrap(consumer, () -> { channel.close(); return null; }, () -> channel.isOpen());
//
//	Subscriber<ByteBuffer> subscriber = new DefaultSubscriber<ByteBuffer>() {
//		@Override
//		public void onNext(ByteBuffer t) {
//			consumer.apply(t);
//		}
//		
//		@Override
//		public void onComplete() {
//			try {
//				channel.close();
//			} catch (IOException | TimeoutException e) {
//				throw new RuntimeException(e);
//			}
//		}
//
//		@Override
//		public void onError(Throwable t) {
//			throw new RuntimeException(t);
//		}
//	};
//			
//	Flowable<ByteBuffer> flowable = wrapChannel(channel, queueName);
//	
//	ChannelWrapper<ByteBuffer> result = new ChannelWrapper<>(subscriber, flowable);
//	return result;
//}
//
