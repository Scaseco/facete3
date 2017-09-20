package org.hobbit.core.services;

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
public abstract class DockerServiceFactoryBase<T extends DockerService>
    implements ServiceFactory<T>
{
    protected String imageName;

    public String getImageName() {
        return imageName;
    }

    public DockerServiceFactoryBase<T> setImageName(String imageName) {
        this.imageName = imageName;
        return this;
    }
}
