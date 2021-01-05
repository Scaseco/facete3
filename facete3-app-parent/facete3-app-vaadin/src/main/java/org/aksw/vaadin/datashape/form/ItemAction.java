package org.aksw.vaadin.datashape.form;

public interface ItemAction<T, C> {
	void invoke(
			T item,
			CompoundConsumer<C> appender,
			CompoundRegistration closeHandler);
}
