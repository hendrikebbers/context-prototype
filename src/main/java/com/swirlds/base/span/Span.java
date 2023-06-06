package com.swirlds.base.span;

public interface Span {

    String getType();

    String getSpanId();

    String getParentSpanId();

    String getData();

    void commit();

    void fail();
}
