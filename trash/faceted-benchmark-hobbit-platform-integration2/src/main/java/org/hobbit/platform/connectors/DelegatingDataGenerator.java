package org.hobbit.platform.connectors;
import org.hobbit.core.components.AbstractDataGenerator;
import org.hobbit.interfaces.DataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;



/**
 * Revised by Claus Stadler 14/09/2017.
 *
 */
public class DelegatingDataGenerator
    extends AbstractDataGenerator
{
    private static final Logger logger = LoggerFactory.getLogger(DelegatingDataGenerator.class);

    protected DataGenerator delegate;

    @Override
    public void init() throws Exception {

        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext(HobbitConfig.class);
        delegate = appContext.getBean(DataGenerator.class);

        super.init();
        logger.info("Data generator initialized");
    }


    @Override
    public void receiveCommand(byte command, byte[] data) {
        delegate.receiveCommand(command, data);
    }


    @Override
    protected void generateData() throws Exception {
        delegate.generateData();
    }
}

