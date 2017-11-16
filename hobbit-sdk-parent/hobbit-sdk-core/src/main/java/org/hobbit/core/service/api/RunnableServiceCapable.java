package org.hobbit.core.service.api;

/**
 * Interface for implementations that act like services
 * that actively do something in a run method.
 *
 *
 *
 *
 * @author raven Sep 24, 2017
 *
 */
public interface RunnableServiceCapable
    extends ServiceCapable
{
    void run() throws Exception;
}
