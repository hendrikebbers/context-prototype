package com.swirlds.base.resources;

import com.swirlds.base.resources.impl.ResourceManagerImpl;

public interface ResourceManager {

    static ResourceManager getInstance() {
        return ResourceManagerImpl.getInstance();
    }

    ExecutorWithSpanSupport getExecutor();

}
