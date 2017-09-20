package org.hobbit.core.services;

import java.util.Map;

/**
 * Base implementation for creating docker services.
 *
 * Calling the .get() method will create a service object whose state is based on the factories current state but independent from it.
 *
 * The factory does not do book keeping of created services.
 *
 *
 * @author raven Sep 20, 2017
 *
 */
public interface DockerServiceFactory<T extends DockerService>
    extends ServiceFactory<T>
{
    String getImageName();
    DockerServiceFactory<T> setImageName(String imageName);


    Map<String, String> getEnvironment();
    DockerServiceFactory<T> setEnvironment(Map<String, String> environment);

}
