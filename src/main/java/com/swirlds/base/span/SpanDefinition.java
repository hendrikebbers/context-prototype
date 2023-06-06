package com.swirlds.base.span;

public record SpanDefinition(String eventType, String label, String description) {

    public SpanDefinition(String eventType) {
        this(eventType, null, null);
    }

    public SpanDefinition(String eventType, String label, String description) {
        this.eventType = eventType;
        this.label = label;
        this.description = description;
    }
}
