package org.hobbit.core.component;

import java.io.InputStream;
import java.util.stream.Stream;

import org.apache.jena.rdf.model.Resource;
import org.hobbit.core.service.api.IdleServiceCapable;

public interface TaskGeneratorModule
	extends DataProtocol
	// This interface conflicts with our spring configuration which only expects on ServiceCapable; fix the way we do config
	//extends IdleServiceCapable
{
	void startUp() throws Exception;
	void shutDown() throws Exception;
	
	void loadDataFromStream(InputStream tmpIn);
	Stream<Resource> generateTasks();
}
