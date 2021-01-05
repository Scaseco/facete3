package org.aksw.vaadin.datashape.form;

import java.util.function.Consumer;

public interface CompoundConsumer<T>
	extends Consumer<T>
{
	void add(Consumer<? super T> consumer);
}
