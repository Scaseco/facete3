package org.hobbit.core.component;

import java.util.function.Consumer;
import java.util.function.Supplier;

import io.reactivex.Flowable;

public class DataPipe<T>
	implements Runnable
{
	protected Supplier<Flowable<T>> dataSource;
	protected Consumer<Supplier<Flowable<T>>> dataSink;
	
	public DataPipe(Supplier<Flowable<T>> dataSource, Consumer<Supplier<Flowable<T>>> dataSink) {
		super();
		this.dataSource = dataSource;
		this.dataSink = dataSink;
	}

	@Override
	public void run() {
		dataSink.accept(dataSource);
	}
}
