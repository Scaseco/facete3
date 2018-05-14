package org.hobbit.benchmark.faceted_browsing.config;

import java.util.function.Consumer;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class BeanHolder<T>
	implements InitializingBean, DisposableBean
{
	protected T bean;
	protected Consumer<? super T> afterPropertiesSetFn;
	protected Consumer<? super T> destroyFn;
	
	public BeanHolder(T bean, Consumer<? super T> afterPropertiesSetFn, Consumer<? super T> destroyFn) {
		super();
		this.bean = bean;
		this.afterPropertiesSetFn = afterPropertiesSetFn;
		this.destroyFn = destroyFn;
	}

	public T getBean() {
		return bean;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if(afterPropertiesSetFn != null) {
			afterPropertiesSetFn.accept(bean);
		}		
	}

	@Override
	public void destroy() throws Exception {
		if(destroyFn != null) {
			destroyFn.accept(bean);
		}		
	}
}
