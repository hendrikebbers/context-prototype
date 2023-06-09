package com.swirlds.base.context.impl;

import com.swirlds.base.context.DiagnosticContext;
import com.swirlds.base.context.DiagnosticContextAccess;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DiagnosticContextImpl implements DiagnosticContext, DiagnosticContextAccess {

    private static final ThreadLocal<DiagnosticContextImpl> threadLocal = new ThreadLocal<>();

    private final Map<String, String> metadata = new HashMap<>();

    public static DiagnosticContextImpl getOrCreateThreadContext() {
        DiagnosticContextImpl threadContext = threadLocal.get();
        if (threadContext == null) {
            threadContext = new DiagnosticContextImpl();
            threadLocal.set(threadContext);
        }
        return threadContext;
    }

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
