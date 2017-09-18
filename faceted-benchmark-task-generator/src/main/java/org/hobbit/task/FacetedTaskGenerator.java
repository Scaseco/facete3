package org.hobbit.task;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Resource;

import org.aksw.jena_sparql_api.core.service.SparqlBasedSystemService;
import org.hobbit.core.components.AbstractSequencingTaskGenerator;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.transfer.StreamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ServiceManager;

/**
 * This is the Task Generator class.
 * @author gkatsimpras
 */
public class FacetedTaskGenerator extends AbstractSequencingTaskGenerator {

}

