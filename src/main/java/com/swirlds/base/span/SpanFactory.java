package com.swirlds.base.span;

public interface SpanFactory {


    default Span create(final String data) {
        return create(null, data);
    }

    default Span create() {
        return create(null, null);
    }

    Span create(final Span parentSpan, final String data);

    default Span create(final Span parentSpan) {
        return create(parentSpan, null);
    }

}
