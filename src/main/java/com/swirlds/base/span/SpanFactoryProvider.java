package com.swirlds.base.span;

import com.swirlds.base.span.impl.SpanFactoryProviderImpl;

public interface SpanFactoryProvider {

    public static SpanFactory createSpanFactory(SpanDefinition spanDefinition) {
        return SpanFactoryProviderImpl.createSpanFactory(spanDefinition);
    }

}
