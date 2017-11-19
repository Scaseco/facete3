package org.hobbit.benchmark.faceted_browsing.main;

//public class MainHobbitFacetedBrowsingBenchmarkRemote
//{
//	private static final Logger logger = LoggerFactory.getLogger(MainHobbitFacetedeBrowsingBenchmarkLocal.class);
//
//	protected static StandardEnvironment environment = new StandardEnvironment();
//	
//
//
//	public static Broker startAmqpBroker() throws Exception {
//    	ConfigurableApplicationContext ctx = SpringApplication.run(ConfigQpidBroker.class);
//
//    	// Presently, the returned broker is already started
//    	Broker result = ctx.getBean(Broker.class);
//    	//result.startup();
//    	return result;
//	}
//
//	
//    public static void main(String[] args) throws Exception {
//
//    	try {
//    		start(args);
//    	} finally {
//    		logger.info("Execution is done.");
//    	}
//    }
//
//    public static void start(String[] args) throws Exception {
//    	
//    	Broker broker = startAmqpBroker();
//
//    	try {
//    		run(args);
//    	} finally {    	
//    		broker.shutdown();
//    	}
//    }
//
//
//    public static void run(String[] args) {
//    	Service systemUnderTestService = null;
//
//    	ConfigurableApplicationContext ctx = null;
//    	try {
//    		
//
//	    	//SpringApplication.run(Application.class, args);
//	        Properties props = new Properties();
//	        props.put("spring.config.location", "classpath:/local-config.properties");
//	        
//	    	ctx = new SpringApplicationBuilder()
//	    		.properties(props)
//	    		.sources(ConfigGson.class)
//	    		.sources(ConfigEncodersFacetedBrowsing.class)
//	        	.sources(ConfigHobbitFacetedBenchmarkLocalServiceMapping.class)
//	        	.sources(HobbitConfigChannelsPlatform.class)
//	        	.sources(ConfigHobbitLocalServices.class)
//	        	.bannerMode(Banner.Mode.OFF)
//	        	.run(args);
//	    	
//
////	    try(AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(
////	            HobbitConfigLocalPlatformFacetedBenchmark.class,
////	            HobbitConfigChannelsPlatform.class,
////	            ConfigHobbitLocalServices.class)) {
//	
//	    	
//    		systemUnderTestService = (Service)ctx.getBean("systemUnderTestService");
//    		systemUnderTestService.startAsync();
//    		systemUnderTestService.awaitRunning();
//    		
////The system adapter has to send out the system_ready_signal
////    		WritableByteChannel commandChannel = (WritableByteChannel)ctx.getBean("commandChannel");
////    		commandChannel.write(ByteBuffer.wrap(new byte[] {Commands.SYSTEM_READY_SIGNAL}));
//    		
//    		
//        	@SuppressWarnings("unchecked")
//			Supplier<Service> systemAdapterServiceFactory = (Supplier<Service>)ctx.getBean("systemAdapterServiceFactory");
//        	Service systemAdapter = systemAdapterServiceFactory.get();
//        	systemAdapter.startAsync();
//        	systemAdapter.awaitRunning();
//        	
//        	
//            PseudoHobbitPlatformController commandHandler = ctx.getBean(PseudoHobbitPlatformController.class);
//            commandHandler.accept(ByteBuffer.wrap(new byte[] {Commands.START_BENCHMARK_SIGNAL}));
//            
//            // Sending the command blocks until the benchmark is complete
//            //System.out.println("sent start benchmark signal");
//            
//    	} finally {
//    		
//            if(systemUnderTestService != null) {
//                logger.debug("Shutting down system under test service");
//                systemUnderTestService.stopAsync();
//                ServiceManagerUtils.awaitTerminatedOrStopAfterTimeout(systemUnderTestService, 60, TimeUnit.SECONDS, 0, TimeUnit.SECONDS);
////              systemUnderTestService.stopAsync();
////              systemUnderTestService.awaitTerminated(60, TimeUnit.SECONDS);
//            }
//            
//            if(ctx != null) {
//            	 Map<String, Channel> channels = ctx.getBeansOfType(Channel.class);
//            	 for(Entry<String, Channel> entry : channels.entrySet()) {
//            		 Channel channel = entry.getValue();
//            		 if(channel.isOpen()) {
//            			 logger.warn("Closing still open channel " + entry.getKey());
//            			 try {
//            				 channel.close();
//            			 } catch(Exception e) {
//            				 logger.warn("Error closing channel", e);
//            			 }
//            		 }
//            	 }
//            }
//    	}
//    }
//}
