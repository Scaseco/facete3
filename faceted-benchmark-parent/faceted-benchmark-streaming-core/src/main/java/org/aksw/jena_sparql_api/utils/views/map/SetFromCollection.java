package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;

public class SetFromCollection<T>
	extends AbstractSet<T>
{
	protected Collection<T> backend;
	
	@Override
	public boolean add(T e) {
		boolean result = backend.contains(e) ? false : backend.add(e);
		return result;
	}
	
	@Override
	public boolean remove(Object o) {
		return backend.remove(o);
	}
	
	@Override
	public boolean contains(Object o) {
		boolean result = backend.contains(o);
		return result;
	}
	
	@Override
	public Iterator<T> iterator() {
		return backend.iterator();
	}

	@Override
	public int size() {
		int result = backend.size();
		return result;
	}
}
