package org.hobbit.core.service.docker;

import java.util.Map;

import org.hobbit.core.service.api.ServiceFactory;

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


    /**
     * Get the local environment of the factory.
     *
     *
     * Local means, that it should NOT return e.g. the system environment
     * or anything else that is outside of control of this factory.
     *
     *
     * We use the term local enviroment in order for subclasses to support spring's Environment object,
     * which is at the core of dependency injenction, and it has a proper API
     * for nesting of property sources
     *
     * Under this consideration, the local environment should be the treated as the first property source
     * of an overall spring environment.
     *
     *
     * @return
     */
    Map<String, String> getLocalEnvironment();

    /**
     *
     * @param environment
     * @return
     */
    DockerServiceFactory<T> setLocalEnvironment(Map<String, String> environment);

}
