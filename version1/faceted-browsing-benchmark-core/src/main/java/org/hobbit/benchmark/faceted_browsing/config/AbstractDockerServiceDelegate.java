package org.hobbit.benchmark.faceted_browsing.config;

import org.hobbit.core.service.docker.api.DockerService;

public class AbstractDockerServiceDelegate<S extends DockerService>
    extends AbstractServiceDelegate<S>
    implements DockerService // TODO There could be default methods for delegation
{
    public AbstractDockerServiceDelegate(S delegate) {
        super(delegate);
    }

    @Override
    public String getImageName() {
        return delegate.getImageName();
    }

    @Override
    public String getContainerId() {
        return delegate.getContainerId();
    }

    @Override
    public Integer getExitCode() {
        return delegate.getExitCode();
    }
    
}