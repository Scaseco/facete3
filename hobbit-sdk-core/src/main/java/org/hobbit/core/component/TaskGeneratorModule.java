package org.hobbit.core.component;

import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;
import org.hobbit.core.service.api.IdleServiceCapable;

public interface TaskGeneratorModule
	extends IdleServiceCapable
{
	void loadDataFromStream(InputStream tmpIn);
	Stream<Resource> generateTasks();
}
