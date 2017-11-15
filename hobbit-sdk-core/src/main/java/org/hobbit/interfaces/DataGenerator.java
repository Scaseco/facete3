package org.hobbit.interfaces;

import org.hobbit.core.component.BaseComponent;

public interface DataGenerator
    extends BaseComponent
{
    void generateData() throws Exception;
}
