package org.hobbit.core.service.api;

public interface ServiceCapable {
    void startUp() throws Exception;
    void shutDown() throws Exception;
}
