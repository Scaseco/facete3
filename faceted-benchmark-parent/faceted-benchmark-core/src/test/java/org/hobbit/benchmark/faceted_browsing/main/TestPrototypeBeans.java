package org.hobbit.benchmark.faceted_browsing.main;

import java.io.Closeable;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import junit.framework.Assert;

/**
 * A spring (boot) test case to verify that prototype beans are properly closed when returned as singleton beans    
 * 
 * @author raven Nov 20, 2017
 *
 */
public class TestPrototypeBeans {
	public static class PrototypeContext {
		public int numClosedBeans = 0;

		@Bean
		@Scope("prototype")
		public Closeable myPrototypeBean() {				
			return () -> { ++numClosedBeans; };
		}
	}
	
	public static class ConcreteContext {
		@Bean
		public Closeable myConcreteBean1(@Qualifier("myPrototypeBean") Closeable bean) {
			return bean;
		}

		@Bean
		public Closeable myConcreteBean2(@Qualifier("myPrototypeBean") Closeable bean) {
			return bean;
		}
	}
	
	
	@Test
	public void test() {
		SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(PrototypeContext.class, ConcreteContext.class);
		
		ConfigurableApplicationContext tmpCtx = appBuilder.run();
		PrototypeContext x;
		try(ConfigurableApplicationContext ctx = tmpCtx) {
			x = tmpCtx.getBean(PrototypeContext.class);
		}		
		
		Assert.assertEquals(x.numClosedBeans, 2);
	}
}
