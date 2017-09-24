package org.hobbit.core.services;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * A ServiceFactory that starts that treats a component class
 * as a pseudo docker container (i.e. a service) that can be started and stopped.
 * Classes are mapped to 'imageNames'.
 *
 * This enables testing of component classes directly
 * that in the production environment run in a separate docker.
 *
 * Note, that there are two levels of services:
 * - The container service itself
 * - Services running inside of the container
 *
 *
 * As for launching the service class:
 * - There has to be a Launcher which is does the initialization.
 *   - On the platform, this is the ComponentStarter
 *
 *
 *
 * TODO This class represents the platform API for creating docker containers
 * Hence, it is this class that is responsible for sending out events if a 'pseudo container' terminates
 *
 *
 *
 * @author raven Sep 24, 2017
 *
 */
public class DockerServiceFactoryClass
    implements DockerServiceFactory<DockerService>
{
    protected Map<String, ServiceFactory<DockerService>> imageNameToServiceFactory;


    // Map of running services
    protected Map<String, DockerService> runningServices;


    public void registerClass(
            String imageName,
            Class<?> clazz,
            BiFunction<Class<?>, Map<String, String>, DockerService> launcher
            )
    {
        imageNameToServiceFactory.put(imageName, null);
    }


    public void DockerService get() {
        // Create a service that wraps the pseudo docker container
        return null;
    }
}


