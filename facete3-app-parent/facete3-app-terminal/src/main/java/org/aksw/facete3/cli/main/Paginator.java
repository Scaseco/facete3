package org.aksw.facete3.cli.main;

import java.util.List;

public interface Paginator<T> {
	List<T> createPages(long numItems, long itemOffset);
}