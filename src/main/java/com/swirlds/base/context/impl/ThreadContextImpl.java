package com.swirlds.base.context.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class ThreadContextImpl {

    private final Map<String, String> metadata = new HashMap<>();

    public void addMetadata(final String name, final String value) {
        metadata.put(name, value);
    }

    public void removeMetadata(final String name) {
        metadata.remove(name);
    }

    public String getValue(final String name) {
        return metadata.get(name);
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(metadata.keySet());
    }
}
