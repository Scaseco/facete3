package org.hobbit.benchmark.faceted_browsing.main;

//public class TestDockerSpringBootAbstractionViaRabbitMq {
//
//	public static class Context {
//		@Bean
//		public ApplicationRunner runner(Environment env) {
//			return (args) -> {
//				System.out.println("Hello " + env.getRequiredProperty("MSG"));
//			};
//		}
//	}
//
//	public static class LauncherContext {
//		
//		
//		
//		@Bean
//		public ApplicationRunner runner(Environment env) {
//			return (args) -> {
//				System.out.println("Launched");
//				//System.out.println("Hello " + env.getRequiredProperty("MSG"));
//			};
//		}
//	}
//
//	@Test
//	public void test() {
//		try(ConfigurableApplicationContext tmpCtx = new SpringApplicationBuilder()
//				.sources(ConfigQpidBroker.class)
//				.sources(ConfigGson.class)
//				.sources(ConfigRabbitMqConnectionFactory.class)
//				.sources(LauncherContext.class)
//				.run()) {
//		}
//
//		
////		
////        Map<String, Class<?>> imageNameToClass = new HashMap<>();
////        imageNameToClass.put("myApp", Context.class);
////        
////        DockerServiceFactory<?> serviceFactory = DockerServiceFactoryUtilsSpringBoot.createDockerServiceFactoryForBootstrap(imageNameToClass,
////        		() -> new SpringApplicationBuilder().bannerMode(Banner.Mode.OFF));
////
////        {
////	        DockerService service = serviceFactory.create("myApp", Collections.singletonMap("MSG", "World1"));
////	        service.startAsync().awaitRunning();
////	        service.stopAsync().awaitTerminated();        
////	        System.out.println("Service had id: " + service.getContainerId());
////        }
////
////        {
////	        DockerService service = serviceFactory.create("myApp", Collections.singletonMap("MSG", "World2"));
////	        service.startAsync().awaitRunning();
////	        service.stopAsync().awaitTerminated();        
////	        System.out.println("Service had id: " + service.getContainerId());
////        }
//	}
//}
