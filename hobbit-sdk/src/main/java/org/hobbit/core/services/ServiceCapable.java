package org.hobbit.core.services;

public interface ServiceCapable {
    void startUp() throws Exception;
    void shutDown() throws Exception;
}
