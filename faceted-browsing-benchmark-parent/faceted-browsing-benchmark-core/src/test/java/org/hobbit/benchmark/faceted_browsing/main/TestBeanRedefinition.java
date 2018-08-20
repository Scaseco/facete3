package org.hobbit.benchmark.faceted_browsing.main;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class TestBeanRedefinition {
	
	private static final Logger logger = LoggerFactory.getLogger(TestBeanRedefinition.class);

	@Configuration
	public static class ContextA {
		public static int myBeanInvocationCount = 0;

		@Bean
		public String myBean() {
			logger.info("ContextA::myBean()");
			++myBeanInvocationCount;
			return "World";
		}
	}
	
	public static class ContextB {
		@Bean
		public String myBean(@Qualifier("myBean") String delegate) {
			return "Hello " + delegate;
		}
	}
	
	@Configuration
	public static class ContextAChild
		extends ContextA
	{
		@Override
		@Bean
		public String myBean() {
			// TODO The magic mentioned below obviously does not work when subclassing a bean method :/
			
			// NOTE Due to spring's magic (or rather: the singleton scope of myBean in the parent class)
			// this call should only return the existing bean
			logger.info("ContextAChild::myBean()");
			String delegate = super.myBean();
			//delegate = super.myBean();
			return "Hello " + delegate;
		}
	}
	
	@Test
	public void testBeanRedefinition() {
		try(ConfigurableApplicationContext ctx = new SpringApplicationBuilder()	
			// .sources(ContextA.class, ContextB.class) // FAILS due to cyclic dependency
			.sources(ContextA.class, ContextAChild.class)
			.run()) {
			String myBean = (String) ctx.getBean("myBean");
			Assert.assertEquals("Hello World", myBean);
			Assert.assertEquals(1, ContextA.myBeanInvocationCount);
		}
	}
}
