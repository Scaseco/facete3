package org.hobbit.benchmark.faceted_browsing.config;

import org.hobbit.core.data.Result;
import org.hobbit.core.storage.Storage;
import org.hobbit.core.storage.StorageInMemory;
import org.springframework.context.annotation.Bean;

public class ConfigEvaluationStorageStorageProvider {
    @Bean
    public Storage<String, Result> storage() {
        return new StorageInMemory<>();
    }
}