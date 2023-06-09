package com.swirlds.base.context;

import java.util.Set;

public interface DiagnosticContextAccess {

    String getValue(final String name);

    Set<String> getKeys();
}
