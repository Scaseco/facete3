package org.hobbit.core.mapview;

import java.util.Iterator;
import java.util.function.Consumer;

public class RemovingIterator<T>
	implements Iterator<T>
{
	protected Iterator<T> delegate;
	protected Consumer<? super T> remover;
	protected T current;

	protected boolean pastStart = false;
		
	public RemovingIterator(Iterator<T> delegate, Consumer<? super T> remover) {
		super();
		this.delegate = delegate;
		this.remover = remover;
	}

	@Override
	public boolean hasNext() {
		boolean result = delegate.hasNext();
		return result;
	}

	@Override
	public T next() {
		pastStart = true;
		current = delegate.next();
		return current;
	}

	@Override
	public void remove() {
		if(!pastStart) {
			throw new IllegalStateException();
		}

		remover.accept(current);
	}
}
