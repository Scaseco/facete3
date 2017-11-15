package org.hobbit.platform.connectors;

import org.hobbit.core.components.AbstractTaskGenerator;
import org.hobbit.interfaces.TaskGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class DelegatingTaskGenerator
    extends AbstractTaskGenerator
{
    private static final Logger logger = LoggerFactory.getLogger(DelegatingTaskGenerator.class);

    protected TaskGenerator delegate;

    @Override
    public void init() throws Exception {

        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext(HobbitConfig.class);
        delegate = appContext.getBean(TaskGenerator.class);

        super.init();
        logger.info("Data generator initialized");
    }


    @Override
    public void receiveCommand(byte command, byte[] data) {
        delegate.receiveCommand(command, data);
    }


    @Override
    protected void generateTask(byte[] data) throws Exception {
        delegate.generateTask(data);
    }

}
