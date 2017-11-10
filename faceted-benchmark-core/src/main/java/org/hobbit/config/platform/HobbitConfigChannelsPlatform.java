package org.hobbit.config.platform;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.hobbit.core.Constants;
import org.hobbit.transfer.PublishingWritableByteChannel;
import org.hobbit.transfer.PublishingWritableByteChannelSimple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;

public class HobbitConfigChannelsPlatform {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HobbitConfigChannelsPlatform.class);
	
    /**
     * Maximum number of retries that are executed to connect to RabbitMQ.
     */
    public static final int NUMBER_OF_RETRIES_TO_CONNECT_TO_RABBIT_MQ = 5;
    /**
     * Time, the system waits before retrying to connect to RabbitMQ. Note that
     * this time will be multiplied with the number of already failed tries.
     */
    public static final long START_WAITING_TIME_BEFORE_RETRY = 5000;
	
	@Inject
	protected Environment env;
		
	protected ConnectionFactory connectionFactory;
	protected String hobbitSessionId;
	
	
    protected Connection incomingDataChannel;
    protected Connection outgoingDataChannel;

    

	@PostConstruct
    public void init() throws Exception {  	
    	hobbitSessionId = env.getProperty(Constants.HOBBIT_SESSION_ID_KEY, Constants.HOBBIT_SESSION_ID_FOR_PLATFORM_COMPONENTS);

        String rabbitMQHostName = env.getProperty(Constants.RABBIT_MQ_HOST_NAME_KEY, "localhost");

        
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitMQHostName);
        connectionFactory.setPort(5672);
        connectionFactory.setAutomaticRecoveryEnabled(true);
        connectionFactory.setVirtualHost("default");
        // attempt recovery every 10 seconds
        connectionFactory.setNetworkRecoveryInterval(10000);
  
        incomingDataChannel = createConnection();
        outgoingDataChannel = createConnection();
	}
	
	
	
    protected Connection createConnection() throws Exception {
    	
    	Connection connection = null;
        for (int i = 0; (connection == null) && (i <= NUMBER_OF_RETRIES_TO_CONNECT_TO_RABBIT_MQ); ++i) {
            try {
                connection = connectionFactory.newConnection();
            } catch (Exception e) {
                if (i < NUMBER_OF_RETRIES_TO_CONNECT_TO_RABBIT_MQ) {
                    long waitingTime = START_WAITING_TIME_BEFORE_RETRY * (i + 1);
                    LOGGER.warn("Couldn't connect to RabbitMQ with try #" + i + ". Next try in " + waitingTime + "ms.",
                            e);
                    try {
                        Thread.sleep(waitingTime);
                    } catch (Exception e2) {
                        LOGGER.warn("Interrupted while waiting before retrying to connect to RabbitMQ.", e2);
                    }
                }
            }
        }
        if (connection == null) {
            String msg = "Couldn't connect to RabbitMQ after " + NUMBER_OF_RETRIES_TO_CONNECT_TO_RABBIT_MQ
                    + " retries.";
            LOGGER.error(msg);
            throw new Exception(msg);
        }
        return connection;
    }

	

        
    public static ChannelWrapper<ByteBuffer> createChannelWrapper(Channel channel, String queueName) throws IOException {
    	String responseQueueName = channel.queueDeclare().getQueue();
    	BasicProperties props = new BasicProperties.Builder()
    			.deliveryMode(2)
    			.replyTo(responseQueueName)
    			.build();
    	
    	
    	Consumer<ByteBuffer> consumer = (buffer) -> {
			try {
				channel.basicPublish(queueName, "", props, buffer.array());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		};

    	//createFlowable(queueFactory, exchangeName);
    	Flowable<ByteBuffer> flowable = wrapChannel(channel, queueName);
    	
    	ChannelWrapper<ByteBuffer> result = new ChannelWrapperImpl<>(consumer, flowable, () -> { channel.close(); return null; });
    	return result;
    }
	
    
    public static Flowable<ByteBuffer> wrapChannel(Channel channel, String queueName) {
        Flowable<ByteBuffer> result = Flowable.create(new FlowableOnSubscribe<ByteBuffer>() {
	        @Override
	        public void subscribe(final FlowableEmitter<ByteBuffer> emitter) throws Exception {
	        	channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
	        		@Override
	        		public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
	        				throws IOException {
	        	    	ByteBuffer buffer = ByteBuffer.wrap(body);
	        	    	emitter.onNext(buffer);
	        		}
	        	});
	        }
	    }, BackpressureStrategy.BUFFER);    	
	
    	return result;
    }
    
    
//    public static Flowable<ByteBuffer> wrapChannel(Channel channel, String queueName) {
//        Flowable<ByteBuffer> result = Flowable.create(new FlowableOnSubscribe<ByteBuffer>() {
//	        @Override
//	        public void subscribe(final FlowableEmitter<ByteBuffer> emitter) throws Exception {
//	        	channel.basicConsume(queueName, true, new DefaultConsumer(channel) {
//	        		@Override
//	        		public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
//	        				throws IOException {
//	        	    	ByteBuffer buffer = ByteBuffer.wrap(body);
//	        	    	emitter.onNext(buffer);
//	        		}
//	        	});
//	        }
//	    }, BackpressureStrategy.BUFFER);    	
//	
//    	return result;
//    }
    
    
    
	
    public static Channel createFanoutChannel(Connection connection, String exchangeName) throws IOException {
        Channel result = connection.createChannel();
        String queueName = result.queueDeclare().getQueue();
        result.exchangeDeclare(exchangeName, "fanout", false, true, null);
        result.queueBind(queueName, exchangeName, "");
    	
        return result;
    }
    
    public static Flowable<ByteBuffer> createFlowable(Connection connection, String exchangeName) throws IOException {
    	Channel channel = createFanoutChannel(connection, exchangeName);
    	String queueName = channel.queueDeclare().getQueue();

    	Flowable<ByteBuffer> result = wrapChannel(channel, queueName);
    	return result;
    }
    
    public static Consumer<ByteBuffer> wrapAsConsumer(Channel channel) throws IOException {

    	String responseQueueName = channel.queueDeclare().getQueue();
    	BasicProperties props = new BasicProperties.Builder()
    			.deliveryMode(2)
    			.replyTo(responseQueueName)
    			.build();

    	Consumer<ByteBuffer> result = (buffer) -> {
    		try {
				channel.basicPublish(Constants.HOBBIT_COMMAND_EXCHANGE_NAME, "", props, buffer.array());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
    	};
    	
        return result;
    }

    @Bean
    public static ChannelWrapper<ByteBuffer> rawCommandChannel(Connection connection) throws IOException {
    	Channel channel = createFanoutChannel(connection, Constants.HOBBIT_COMMAND_EXCHANGE_NAME);
    	ChannelWrapper<ByteBuffer> result = createChannelWrapper(channel, Constants.HOBBIT_COMMAND_EXCHANGE_NAME);
    	return result;
    }

    @Bean
    public static Consumer<ByteBuffer> commandChannel(@Qualifier("rawCommandChannel") ChannelWrapper<ByteBuffer> channelWrapper) throws IOException {
    	return channelWrapper.getConsumer();
    }
        
 
    @Bean
    public Flowable<ByteBuffer> commandPub(@Qualifier("rawCommandChannel") ChannelWrapper<ByteBuffer> channelWrapper) {
    	return channelWrapper.getFlowable();
    }

   
    public String getHobbitSessionId() {
        return hobbitSessionId;
    }

    public String generateSessionQueueName(String queueName) {
        return queueName + "." + hobbitSessionId;
    }

    
//    public ChannelWrapper<ByteBuffer> buildSender(String queueName) {
////    	String sessionQueueName = generateSessionQueueName(queueName); 
//    	
//    	
////        DataSender sender2TaskGen = DataSenderImpl.builder().queue(outgoingQueueFactory,
////                generateSessionQueueName(Constants.DATA_GEN_2_TASK_GEN_QUEUE_NAME)).build();
////    	
//        //DataSenderImpl.Builder
//        
//    	
//    }
    

    @Bean(name = { "dg2tg", "dg2tgPub" })
    public PublishingWritableByteChannel dg2tg() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "dg2sa", "dg2saPub" })
    public PublishingWritableByteChannel dg2sa() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "tg2sa", "tg2saPub" })
    public PublishingWritableByteChannel tg2sa() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "tg2es", "tg2esPub" })
    public PublishingWritableByteChannel tg2es() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "sa2es", "sa2esPub" })
    public PublishingWritableByteChannel sa2es() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "es2em", "es2emPub" })
    public PublishingWritableByteChannel es2em() {
        return new PublishingWritableByteChannelSimple();
    }

    @Bean(name = { "em2es", "em2esPub" })
    public PublishingWritableByteChannel em2es() {
        return new PublishingWritableByteChannelSimple();
        //return new PublishingWritableByteChannelQueued();
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
