package com.swirlds.base.context.impl;

import com.swirlds.base.context.DiagnosticContextAccess;

public class DiagnosticContextAccessProvider {

    public static DiagnosticContextAccess getAccess() {
        return DiagnosticContextImpl.getOrCreateThreadContext();
    }
}
