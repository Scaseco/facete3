package org.aksw.vaadin.datashape.form;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class CompoundConsumerImpl<T>
	implements CompoundConsumer<T>
{
	protected Set<Consumer<? super T>> consumers = new LinkedHashSet<>();
	
	@Override
	public void accept(T arg) {
		consumers.forEach(item -> item.accept(arg));
	}

	@Override
	public void add(Consumer<? super T> consumer) {
		Objects.requireNonNull(consumer);
		consumers.add(consumer);
	}

}
