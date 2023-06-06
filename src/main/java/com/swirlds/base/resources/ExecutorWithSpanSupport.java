package com.swirlds.base.resources;

import com.swirlds.base.span.Span;
import com.swirlds.base.span.SpanDefinition;
import java.util.concurrent.Executor;

public interface ExecutorWithSpanSupport extends Executor {

    default void execute(Runnable command, SpanDefinition childSpanDefinition, String data) {
        execute(command, childSpanDefinition, data, null);
    }

    default void execute(Runnable command, SpanDefinition childSpanDefinition) {
        execute(command, childSpanDefinition, null, null);
    }

    default void execute(Runnable command, SpanDefinition childSpanDefinition, Span parentSpan) {
        execute(command, childSpanDefinition, null, parentSpan);
    }

    default void execute(Runnable command) {
        execute(command, null, null, null);
    }

    void execute(Runnable command, SpanDefinition childSpanDefinition, String data, Span parentSpan);

}
