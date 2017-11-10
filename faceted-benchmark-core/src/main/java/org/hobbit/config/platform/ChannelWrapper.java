package org.hobbit.config.platform;

import java.io.Closeable;
import java.util.function.Consumer;

import io.reactivex.Flowable;

public interface ChannelWrapper<T>
	extends Closeable
{
	/**
	 * Get the consumer which enables putting data into the channel
	 * 
	 * @return
	 */
	Consumer<T> getConsumer();


	/**
	 * Get the flowable which enables notifications of messages on the channel
	 * @return
	 */
	Flowable<T> getFlowable();
}
