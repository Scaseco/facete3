package org.hobbit.core.services;

/**
 * Tag interface to indicate that a service
 * can be started and stopped, however
 * does not need a run method
 *
 * @author raven Sep 24, 2017
 *
 */
public interface IdleServiceCapable
    extends ServiceCapable
{

}
