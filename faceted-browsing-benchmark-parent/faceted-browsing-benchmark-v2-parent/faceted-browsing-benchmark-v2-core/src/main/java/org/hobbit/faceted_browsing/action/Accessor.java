package org.hobbit.faceted_browsing.action;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface Accessor<T>
	extends Supplier<T>, Consumer<T>
{

}
