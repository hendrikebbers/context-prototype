package com.swirlds.base.context;

import com.swirlds.base.context.impl.DiagnosticContextImpl;

public interface DiagnosticContext {
    
    static DiagnosticContext getCurrent() {
        return DiagnosticContextImpl.getOrCreateThreadContext();
    }

    void addMetadata(final String name, final String value);

    void removeMetadata(final String name);
}
